#!/bin/sh
set -eu

repository_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
apebind_bin=${APEBIND_BIN:-apebind}
temporary_root=$(mktemp -d)
trap 'rm -rf "$temporary_root"' EXIT HUP INT TERM

"$repository_root/scripts/prepare.sh"
"$apebind_bin" validate "$repository_root/rg.apebind.yaml"

for language in python node java; do
  "$apebind_bin" generate "$repository_root/rg.apebind.yaml" \
    --ape "$repository_root/rg.com" \
    --lang "$language" \
    -o "$temporary_root/$language"
done

python_src="$repository_root/bindings/python/src/rg_ape"
node_src="$repository_root/bindings/node/src"
java_src="$repository_root/bindings/java/src/main/java/apebind/generated/rg_ape"

cp "$temporary_root/python/src/rg_ape/_generated.py" "$python_src/"
cp "$temporary_root/python/src/rg_ape/_runtime.py" "$python_src/"
cp "$temporary_root/python/src/rg_ape/py.typed" "$python_src/"

cp "$temporary_root/node/src/client.js" "$node_src/"
cp "$temporary_root/node/src/client.d.ts" "$node_src/"
cp "$temporary_root/node/src/_runtime.js" "$node_src/"
cp "$temporary_root/node/src/_runtime.d.ts" "$node_src/"

cp "$temporary_root/java/src/main/java/apebind/generated/rg_ape/"*.java "$java_src/"

printf '%s\n' 'Generated transport bindings refreshed; semantic APIs and package metadata left untouched.'
