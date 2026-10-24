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

    def test_pcre2_backreference_matches(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "dup.txt"
            note.write_text("ape ape\nbeta\n", encoding="utf-8")
            text = search(r"(\w+) \1", directory, line_number=True, pcre2=True)

        self.assertIn("dup.txt:1:ape ape", text)

    def test_no_match_raises(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "note.txt"
            note.write_text("beta\n", encoding="utf-8")
            with self.assertRaises(APEProcessError):
                search("ape", directory)

    def test_fixed_strings_matches_the_literal(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "lit.txt"
            note.write_text("abc\na.c\n", encoding="utf-8")
            text = search("a.c", directory, line_number=True, fixed_strings=True)

        self.assertIn("lit.txt:2:a.c", text)
        self.assertNotIn("lit.txt:1:abc", text)

    def test_word_regexp_skips_longer_words(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "words.txt"
            note.write_text("alpha ape\napex\n", encoding="utf-8")
            text = search("ape", directory, line_number=True, word_regexp=True)

        self.assertIn("words.txt:1:alpha ape", text)
        self.assertNotIn("words.txt:2:apex", text)

    def test_context_lines(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "note.txt"
            note.write_text("one\ntwo\nthree\nfour\n", encoding="utf-8")
            after = search("two", directory, line_number=True, after_context=1)
            before = search("two", directory, line_number=True, before_context=1)
            both = search("two", directory, line_number=True, context=1)

        self.assertIn("note.txt:2:two", after)
        self.assertIn("note.txt-3-three", after)
        self.assertNotIn("note.txt-1-one", after)
        self.assertIn("note.txt-1-one", before)
        self.assertIn("note.txt:2:two", before)
        self.assertNotIn("note.txt-3-three", before)
        self.assertIn("note.txt-1-one", both)
        self.assertIn("note.txt:2:two", both)
        self.assertIn("note.txt-3-three", both)
        self.assertNotIn("note.txt-4-four", both)

    def test_file_type_limits_the_search(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "code.py").write_text("needle\n", encoding="utf-8")
            (root / "code.txt").write_text("needle\n", encoding="utf-8")
            text = search("needle", directory, line_number=True, file_type="py")

        self.assertIn("code.py:1:needle", text)
        self.assertNotIn("code.txt", text)

    def test_invert_match_prints_other_lines(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "note.txt"
            note.write_text("alpha ape\nbeta\n", encoding="utf-8")
            text = search("ape", directory, line_number=True, invert_match=True)

        self.assertIn("note.txt:2:beta", text)
        self.assertNotIn("alpha ape", text)

    def test_raw_argv_returns_the_same_text(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "lit.txt"
            note.write_text("abc\na.c\n", encoding="utf-8")
            named = search("a.c", directory, line_number=True, fixed_strings=True)
            keyword = search(raw=["-n", "-F", "a.c", directory])
            positional = search(["-n", "-F", "a.c", directory])

        self.assertEqual(keyword, named)
        self.assertEqual(positional, named)

    def test_raw_no_match_raises(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "note.txt"
            note.write_text("beta\n", encoding="utf-8")
            with self.assertRaises(APEProcessError):
                search(raw=["missing-token", directory])

    def test_parameters_and_raw_together_raise(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "note.txt"
            note.write_text("alpha ape\n", encoding="utf-8")
            with self.assertRaises(TypeError):
                search("ape", directory, raw=["ape", directory])
            with self.assertRaises(TypeError):
                search(raw=["ape", directory], line_number=False)
            with self.assertRaises(TypeError):
                search(["-n", "ape", directory], line_number=False)


if __name__ == "__main__":
    unittest.main()
