#!/bin/sh
set -eu

script_root=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_root=$(CDPATH= cd -- "$script_root/.." && pwd)
classes=$(mktemp -d)
trap 'rm -rf "$classes"' EXIT

"$script_root/prepare.sh"
javac --release 17 -d "$classes" \
  "$project_root/src/main/java/apebind/generated/rg_ape/"*.java \
  "$project_root/tests/RgSmoke.java"
java -cp "$classes:$project_root/src/main/resources" RgSmoke
