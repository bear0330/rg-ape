#!/bin/sh
set -eu

repository_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)

"$repository_root/scripts/prepare.sh"
python3 "$repository_root/tests/test_rg_com.py"
"$repository_root/bindings/python/scripts/test.sh"
"$repository_root/bindings/node/scripts/test.sh"
"$repository_root/bindings/java/scripts/test.sh"
