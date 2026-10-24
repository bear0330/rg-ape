# SPDX-License-Identifier: MIT

from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from rg_ape import APEProcessError, search


class RgApeTests(unittest.TestCase):
    def test_search_prints_the_matching_line(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "note.txt"
            note.write_text("alpha ape\nbeta\n", encoding="utf-8")
            text = search("ape", directory, line_number=True)

        self.assertIn("note.txt:1:alpha ape", text)

    def test_no_match_raises(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "note.txt"
            note.write_text("beta\n", encoding="utf-8")
            with self.assertRaises(APEProcessError):
                search("ape", directory)


if __name__ == "__main__":
    unittest.main()
