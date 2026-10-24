#!/bin/sh
set -eu

script_root=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

"$script_root/prepare.sh"
node "$script_root/../tests/test_rg_ape.mjs"
