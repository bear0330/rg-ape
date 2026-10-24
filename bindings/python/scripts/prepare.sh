#!/bin/sh
set -eu

project_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
repository_root=$(CDPATH= cd -- "$project_root/../.." && pwd)
source_ape="$repository_root/rg.com"
package_ape="$project_root/src/rg_ape/bin/rg.com"

"$repository_root/scripts/prepare.sh"

mkdir -p "$(dirname -- "$package_ape")"
if [ ! -f "$package_ape" ] || ! cmp -s "$source_ape" "$package_ape"; then
  cp "$source_ape" "$package_ape"
fi
chmod +x "$package_ape"
