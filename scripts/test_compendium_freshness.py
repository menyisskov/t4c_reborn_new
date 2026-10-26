import json
from pathlib import Path
import tempfile
import unittest

from compendium_freshness import stamp, verify


class CompendiumFreshnessTest(unittest.TestCase):
    def test_unchanged_exports_keep_their_date_and_changed_exports_require_a_new_stamp(self):
        with tempfile.TemporaryDirectory() as directory:
            site = Path(directory)
            (site / "data").mkdir()
            (site / "data.js").write_text('window.T4C_DATA = {};\n', encoding="utf-8")
            first = stamp(site)
            self.assertEqual(first, stamp(site))
            self.assertEqual(first, verify(site))
            (site / "data.js").write_bytes((site / "data.js").read_text(encoding="utf-8").replace("\n", "\r\n").encode("utf-8"))
            self.assertEqual(first, stamp(site), "Git newline conversion must not change the snapshot")
            (site / "data.js").write_text('window.T4C_DATA = {"items": []};\n', encoding="utf-8")
            (site / "data" / "items.json").write_text('[]', encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "without refreshing"):
                verify(site)
            second = stamp(site)
            self.assertNotEqual(first["dataSha256"], second["dataSha256"])
            self.assertEqual(second, verify(site))

    def test_snapshot_with_a_stale_browser_bundle_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            site = Path(directory)
            (site / "data").mkdir()
            (site / "data.js").write_text('window.T4C_DATA = {"items": []};\n', encoding="utf-8")
            (site / "data" / "items.json").write_text(json.dumps([{"key": "new-item"}]), encoding="utf-8")
            stamp(site)
            with self.assertRaisesRegex(ValueError, "Browser bundle differs"):
                verify(site)


if __name__ == "__main__":
    unittest.main()
