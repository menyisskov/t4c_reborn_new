import hashlib
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


if __name__ == "__main__":
    unittest.main()
