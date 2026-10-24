# SPDX-License-Identifier: MIT

from __future__ import annotations

import errno
import subprocess
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RG = ROOT / "rg.com"


class RgComTests(unittest.TestCase):
    def run_rg(self, *arguments: str) -> subprocess.CompletedProcess[str]:
        command = [str(RG), *arguments]
        try:
            return subprocess.run(
                command,
                check=False,
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
            )
        except OSError as exc:
            if exc.errno != errno.ENOEXEC:
                raise
            return subprocess.run(
                ["/bin/bash", "-c", 'exec "$0" "$@"', *command],
                check=False,
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
            )

    def test_version(self) -> None:
        result = self.run_rg("--version")

        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("ripgrep 15.2.0", result.stdout)

    def test_search_prints_the_matching_line(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "note.txt"
            note.write_text("alpha ape\nbeta\n", encoding="utf-8")
            result = self.run_rg("-n", "ape", directory)

        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("note.txt:1:alpha ape", result.stdout)

    def test_no_match_exits_nonzero(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            note = Path(directory) / "note.txt"
            note.write_text("beta\n", encoding="utf-8")
            result = self.run_rg("ape", directory)

        self.assertEqual(result.returncode, 1)
        self.assertEqual(result.stdout, "")


if __name__ == "__main__":
    unittest.main()
