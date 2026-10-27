import copy
import hashlib
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import verify_backup as backup


class BackupVerificationTest(unittest.TestCase):
    def test_restore_names_cannot_select_source_or_inject_connection_options(self):
        self.assertEqual("t4c_restore_qa_20261027", backup.database_name("t4c_restore_qa_20261027", True))
        for name in ("t4c_online", "t4c_restore_x dbname=t4c_online", "t4c_restore_" + "x" * 60):
            with self.assertRaises(backup.VerificationError):
                backup.database_name(name, True)
        self.assertEqual('"a""b"', backup.identifier('a"b'))

    def test_streamed_copy_digest_counts_rows_without_retaining_values(self):
        # Literal newlines delimit rows; escaped newlines inside values do not.
        data = b"1\tfirst\\nsecond\n2\t\\N\n"
        self.assertEqual({"rows": 2, "sha256": hashlib.sha256(data).hexdigest()},
                         backup.digest_chunks([data[:9], data[9:18], data[18:]]))
        with self.assertRaises(backup.VerificationError):
            backup.compare([{"name": "online_x"}], [{"name": "online_x"}],
                           {"online_x": {"rows": 2, "sha256": "a"}},
                           {"online_x": {"rows": 2, "sha256": "b"}})

    def test_existing_database_failure_never_starts_dump_or_restore(self):
        class Collision:
            def __init__(self):
                self.calls = []

            def run(self, program, *arguments, stage):
                self.calls.append((program, arguments))
                raise backup.VerificationError("Fresh restore database creation failed")

        postgres = Collision()
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "new-backup"
            with patch.object(backup, "ExportedSnapshot") as snapshot:
                with self.assertRaises(backup.VerificationError):
                    backup.verify(postgres, "t4c_online", "t4c_restore_existing", output)
                snapshot.assert_not_called()
        self.assertEqual(1, len(postgres.calls))
        program, arguments = postgres.calls[0]
        self.assertEqual("createdb", program)
        self.assertEqual("t4c_restore_existing", arguments[-1])
        self.assertNotIn("--clean", arguments)

    @unittest.skipUnless(os.environ.get("T4C_BACKUP_TEST_SOURCE") and
                         os.environ.get("T4C_BACKUP_TEST_RESTORE"),
                         "requires private PostgreSQL backup fixture")
    def test_postgres_reparses_equivalent_checks_but_rejects_weakened_or_missing_check(self):
        postgres = backup.Postgres(os.environ.get("T4C_BACKUP_TEST_PG_BIN"), 60)
        source = backup.catalog(postgres, os.environ["T4C_BACKUP_TEST_SOURCE"], None)
        target_name = os.environ["T4C_BACKUP_TEST_RESTORE"]
        restored = backup.catalog(postgres, target_name, None)
        self.assertEqual("postgresql_temporary_check_parse",
                         backup.compare(source, restored, {}, {}, postgres, target_name))

        weakened = copy.deepcopy(source)
        loot = next(table for table in weakened if table["name"] == "online_ground_loot")
        state_check = next(index for index, definition in enumerate(loot["constraints"])
                           if "ON_GROUND" in definition)
        loot["constraints"][state_check] = "CHECK (true)"
        with self.assertRaises(backup.VerificationError):
            backup.compare(weakened, restored, {}, {}, postgres, target_name)

        missing = copy.deepcopy(source)
        next(table for table in missing if table["name"] == "online_ground_loot")[
            "constraints"].pop(state_check)
        with self.assertRaises(backup.VerificationError):
            backup.compare(missing, restored, {}, {}, postgres, target_name)

        changed_unique = copy.deepcopy(source)
        loot = next(table for table in changed_unique if table["name"] == "online_ground_loot")
        unique_index = next(index for index, definition in enumerate(loot["constraints"])
                            if definition.startswith("UNIQUE ("))
        loot["constraints"][unique_index] = "UNIQUE (id)"
        with self.assertRaises(backup.VerificationError):
            backup.compare(changed_unique, restored, {}, {}, postgres, target_name)

        with self.assertRaises(backup.VerificationError):
            backup.compare(source, restored, {"online_x": {"rows": 1, "sha256": "a"}},
                           {"online_x": {"rows": 1, "sha256": "b"}}, postgres, target_name)


if __name__ == "__main__":
    unittest.main()
