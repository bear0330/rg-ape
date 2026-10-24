#!/bin/sh
set -eu

script_root=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_root=$(CDPATH= cd -- "$script_root/.." && pwd)

"$script_root/prepare.sh"
PYTHONPATH="$project_root/src${PYTHONPATH:+:$PYTHONPATH}" \
  python3 "$project_root/tests/test_rg_ape.py"
