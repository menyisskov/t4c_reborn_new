#!/usr/bin/env python3
"""Consistent online-realm backup/restore drill. Uses Python stdlib and PostgreSQL tools."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import queue
import re
import subprocess
import sys
import threading
import time


class VerificationError(Exception):
    """Safe operator-facing error, with no database output or connection secrets."""


def database_name(value, restore=False):
    pattern = r"t4c_restore_[a-z0-9_]+" if restore else r"[A-Za-z_][A-Za-z0-9_]*"
    if len(value) > 63 or not re.fullmatch(pattern, value):
        raise VerificationError("Invalid restore database name" if restore else "Invalid source database name")
    return value


def identifier(value):
    return '"' + value.replace('"', '""') + '"'


def snapshot_prefix(snapshot):
    if not re.fullmatch(r"[0-9A-Fa-f]+-[0-9A-Fa-f]+-[0-9]+", snapshot):
        raise VerificationError("Invalid exported snapshot identifier")
    return "BEGIN ISOLATION LEVEL REPEATABLE READ READ ONLY; SET TRANSACTION SNAPSHOT '" + snapshot + "'; "


def digest_chunks(chunks):
    digest, count = hashlib.sha256(), 0
    for chunk in chunks:
        digest.update(chunk)
        # PostgreSQL COPY text escapes embedded LF, so one literal LF is exactly one row.
        count += chunk.count(b"\n")
    return {"rows": count, "sha256": digest.hexdigest()}


class Postgres:
    def __init__(self, pg_bin, timeout):
        self.pg_bin = Path(pg_bin) if pg_bin else None
        self.timeout = timeout
        self.environment = dict(os.environ)
        self.environment["PGCONNECT_TIMEOUT"] = "10"
        self.environment["PGCLIENTENCODING"] = "UTF8"
        # Canonical COPY text formatting, independent of database/role/session defaults.
        self.environment["PGOPTIONS"] = (
            "-c timezone=UTC -c datestyle=ISO,YMD -c intervalstyle=postgres "
            "-c extra_float_digits=3 -c bytea_output=hex -c search_path=pg_catalog "
            f"-c statement_timeout={timeout * 1000} -c lock_timeout=10000"
        )

    def command(self, program, *arguments):
        return [str(self.pg_bin / program) if self.pg_bin else program, *arguments]

    def run(self, program, *arguments, stage):
        try:
            result = subprocess.run(self.command(program, *arguments), env=self.environment,
                                    stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
                                    timeout=self.timeout, check=False)
        except (OSError, subprocess.TimeoutExpired):
            raise VerificationError(stage + " could not complete") from None
        if result.returncode:
            raise VerificationError(stage + " failed; database errors are suppressed to protect credentials/data")
        return result.stdout

    def query(self, database, statement, snapshot=None):
        sql = (snapshot_prefix(snapshot) if snapshot else "") + statement
        return self.run("psql", "-X", "-q", "-A", "-t", "--no-password", "--set=ON_ERROR_STOP=1",
                        "--dbname", database, "--command", sql, stage="Database metadata read")

    def stream(self, database, statement, snapshot=None):
        sql = (snapshot_prefix(snapshot) if snapshot else "") + statement
        try:
            process = subprocess.Popen(self.command("psql", "-X", "-q", "--no-password",
                                        "--set=ON_ERROR_STOP=1", "--dbname", database, "--command", sql),
                                       env=self.environment, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
        except OSError:
            raise VerificationError("Table verification could not start") from None
        timer = threading.Timer(self.timeout, process.kill)
        timer.start()
        try:
            result = digest_chunks(iter(lambda: process.stdout.read(1024 * 1024), b""))
            if process.wait():
                raise VerificationError("Table verification failed or timed out")
            return result
        finally:
            timer.cancel()
            process.stdout.close()
            if process.poll() is None:
                process.kill()
                process.wait()


class ExportedSnapshot:
    """Hold the exporting transaction open until dump and all source reads finish."""
    def __init__(self, postgres, database):
        self.postgres, self.database, self.process = postgres, database, None

    def __enter__(self):
        try:
            self.process = subprocess.Popen(self.postgres.command("psql", "-X", "-q", "-A", "-t",
                                             "--no-password", "--set=ON_ERROR_STOP=1", "--dbname", self.database),
                                            env=self.postgres.environment, stdin=subprocess.PIPE,
                                            stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True)
            self.process.stdin.write("BEGIN ISOLATION LEVEL REPEATABLE READ READ ONLY; SELECT pg_export_snapshot();\n")
            self.process.stdin.flush()
            result = queue.Queue(maxsize=1)
            threading.Thread(target=lambda: result.put(self.process.stdout.readline(256)), daemon=True).start()
            snapshot = result.get(timeout=min(20, self.postgres.timeout)).strip()
            snapshot_prefix(snapshot)
            return snapshot
        except (OSError, queue.Empty, VerificationError):
            self.__exit__(None, None, None)
            raise VerificationError("Could not export a source snapshot") from None

    def __exit__(self, *_):
        if self.process is not None:
            try:
                self.process.communicate("ROLLBACK;\n", timeout=5)
            except (OSError, subprocess.TimeoutExpired, ValueError):
                if self.process.poll() is None:
                    self.process.kill()
                    self.process.wait()


CATALOG_SQL = """
SELECT COALESCE(json_agg(t ORDER BY t.name), '[]'::json) FROM (
  SELECT c.relname AS name,
    (SELECT json_agg(a.attname ORDER BY k.ordinality)
       FROM pg_index i CROSS JOIN LATERAL unnest(i.indkey) WITH ORDINALITY k(attnum, ordinality)
       JOIN pg_attribute a ON a.attrelid=i.indrelid AND a.attnum=k.attnum
       WHERE i.indrelid=c.oid AND i.indisprimary) AS primary_key,
    (SELECT json_agg(json_build_object('name',a.attname,'type',format_type(a.atttypid,a.atttypmod),
                    'not_null',a.attnotnull,'default',pg_get_expr(d.adbin,d.adrelid)) ORDER BY a.attnum)
       FROM pg_attribute a LEFT JOIN pg_attrdef d ON d.adrelid=a.attrelid AND d.adnum=a.attnum
       WHERE a.attrelid=c.oid AND a.attnum>0 AND NOT a.attisdropped) AS columns,
    (SELECT json_agg(pg_get_constraintdef(k.oid) ORDER BY k.conname)
       FROM pg_constraint k WHERE k.conrelid=c.oid) AS constraints
  FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
  WHERE n.nspname='public' AND left(c.relname,7)='online_' AND c.relkind IN ('r','p')
) t;
"""


def catalog(postgres, database, snapshot):
    try:
        tables = json.loads(postgres.query(database, CATALOG_SQL, snapshot))
    except (ValueError, UnicodeError):
        raise VerificationError("Invalid table metadata") from None
    if not tables or not any(table["name"] == "online_schema" for table in tables):
        raise VerificationError("Source does not contain an online realm schema")
    if any(not table["primary_key"] for table in tables):
        raise VerificationError("Every online table must have a primary key for deterministic verification")
    return tables


def table_digests(postgres, database, tables, snapshot):
    result = {}
    for table in tables:
        ordering = ", ".join(identifier(column) + '::text COLLATE "C"' for column in table["primary_key"])
        sql = f'COPY (SELECT * FROM "public".{identifier(table["name"])} ORDER BY {ordering}) TO STDOUT;'
        result[table["name"]] = postgres.stream(database, sql, snapshot)
    return result


def checks_match_after_postgres_parse(postgres, database, source_tables, restored_tables):
    """Reparse only differing CHECKs on empty temporary clones in the restore DB."""
    if len(source_tables) != len(restored_tables):
        return False
    differences = []
    for source, restored in zip(source_tables, restored_tables):
        if ({key: value for key, value in source.items() if key != "constraints"} !=
                {key: value for key, value in restored.items() if key != "constraints"}):
            return False
        source_constraints, restored_constraints = source["constraints"], restored["constraints"]
        if len(source_constraints) != len(restored_constraints):
            return False
        for original, recovered in zip(source_constraints, restored_constraints):
            if original == recovered:
                continue
            if not original.startswith("CHECK (") or not recovered.startswith("CHECK ("):
                return False
            differences.append((restored["name"], original, recovered))
    if not differences:
        return True
    if len(differences) > 128:
        raise VerificationError("Too many differing CHECK constraints to validate safely")

    statements = ["BEGIN;"]
    expressions = []
    for index, (table, original, recovered) in enumerate(differences):
        for side, definition in (("source", original), ("restore", recovered)):
            if len(definition) > 8192:
                raise VerificationError("CHECK constraint too large to validate safely")
            clone = identifier(f"backup_check_{side}_{index}")
            statements.append(f"CREATE TEMP TABLE {clone} (LIKE public.{identifier(table)});")
            statements.append(f"ALTER TABLE pg_temp.{clone} ADD CONSTRAINT backup_check {definition};")
            expressions.append(
                "(SELECT pg_get_constraintdef(oid) FROM pg_constraint "
                f"WHERE conrelid = 'pg_temp.{clone}'::regclass AND conname = 'backup_check')"
            )
    statements.append("SELECT json_build_array(" + ", ".join(expressions) + ");")
    statements.append("ROLLBACK;")
    try:
        canonical = json.loads(postgres.query(database, "\n".join(statements)))
    except (ValueError, UnicodeError):
        raise VerificationError("Invalid PostgreSQL CHECK comparison result") from None
    if len(canonical) != len(expressions) or any(value is None for value in canonical):
        raise VerificationError("Incomplete PostgreSQL CHECK comparison result")
    return all(canonical[index] == canonical[index + 1]
               for index in range(0, len(canonical), 2))


def compare(source_tables, restored_tables, source_data, restored_data, postgres=None, database=None):
    schema_comparison = "exact"
    if source_tables != restored_tables:
        if postgres is None or database is None or not checks_match_after_postgres_parse(
                postgres, database, source_tables, restored_tables):
            raise VerificationError("Restored online table definitions do not match the exported snapshot")
        schema_comparison = "postgresql_temporary_check_parse"
    if source_data != restored_data:
        raise VerificationError("Restored online row counts or hashes do not match the exported snapshot")
    return schema_comparison


def verify(postgres, source, target, output):
    database_name(source)
    database_name(target, restore=True)
    if source == target:
        raise VerificationError("Source and restore databases must differ")
    output = Path(output).expanduser().resolve()
    try:
        output.mkdir(mode=0o700, parents=True, exist_ok=False)
    except OSError:
        raise VerificationError("Output directory must be new and writable") from None
    archive = output / "realm.dump"
    # CREATE DATABASE itself is the collision guard. No prior existence check/racy overwrite.
    postgres.run("createdb", "--no-password", "--maintenance-db=postgres", "--template=template0", target,
                 stage="Fresh restore database creation (existing databases are never reused)")
    started = time.monotonic()
    with ExportedSnapshot(postgres, source) as snapshot:
        source_tables = catalog(postgres, source, snapshot)
        postgres.run("pg_dump", "--no-password", "--format=custom", "--snapshot=" + snapshot,
                     "--dbname", source, "--file", str(archive), stage="Consistent backup")
        source_data = table_digests(postgres, source, source_tables, snapshot)
    postgres.run("pg_restore", "--no-password", "--exit-on-error", "--single-transaction",
                 "--no-owner", "--no-privileges", "--dbname", target, str(archive), stage="Fresh database restore")
    with ExportedSnapshot(postgres, target) as restored_snapshot:
        restored_tables = catalog(postgres, target, restored_snapshot)
        restored_data = table_digests(postgres, target, restored_tables, restored_snapshot)
    schema_comparison = compare(source_tables, restored_tables, source_data, restored_data,
                                postgres, target)
    with archive.open("rb") as stream:
        archive_hash = hashlib.file_digest(stream, "sha256").hexdigest()
    report = {"verified": True, "source_database": source, "restore_database": target,
              "source_snapshot": snapshot, "archive_sha256": archive_hash,
              "elapsed_seconds": round(time.monotonic() - started, 3),
              "schema_comparison": schema_comparison,
              "tables": source_data, "native_playthrough_verified": False}
    report_path = output / "verification.json"
    report_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    return report_path


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", required=True, help="Source database name; connection uses PG* environment")
    parser.add_argument("--restore", required=True, help="Fresh t4c_restore_* database name (never reused/dropped)")
    parser.add_argument("--output-dir", required=True, help="New private output directory for archive/report")
    parser.add_argument("--pg-bin", help="Directory containing psql, pg_dump, pg_restore and createdb")
    parser.add_argument("--timeout", type=int, default=600, help="Maximum seconds per subprocess (default 600)")
    args = parser.parse_args()
    try:
        if os.name != "posix":
            raise VerificationError("Run inside WSL/Linux/macOS so private archive permissions can be enforced")
        if not 10 <= args.timeout <= 3600:
            raise VerificationError("Timeout must be between 10 and 3600 seconds")
        report = verify(Postgres(args.pg_bin, args.timeout), args.source, args.restore, args.output_dir)
        print(f"Backup and restored online table hashes verified. Report: {report}")
        print("Restore database retained. Native playthrough is still required.")
        return 0
    except VerificationError as error:
        print(str(error), file=sys.stderr)
        print("No database was dropped. Any created restore database/artifacts are retained for inspection.", file=sys.stderr)
        return 1
    except OSError:
        print("Backup artifact I/O failed. No database was dropped; created artifacts are retained.", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
