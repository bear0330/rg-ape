#!/bin/sh
# Copy an rg.com built by rust-ape into the generated binding packages.
set -eu

root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
src=${RG_APE_RG:-}

if [ -z "$src" ] || [ ! -f "$src" ]; then
  printf '%s\n' 'Set RG_APE_RG to the rg.com produced by rust-ape collectbuild cli/rg' >&2
  exit 1
fi

install_binary() {
  destination=$1
  mkdir -p "$(dirname -- "$destination")"
  cp -f "$src" "$destination"
  chmod +x "$destination"
}

install_binary "$root/bindings/python/src/rg_ape/bin/rg.com"
install_binary "$root/bindings/node/src/bin/rg.com"
install_binary "$root/bindings/java/src/main/resources/apebind/rg.com"
printf 'Installed %s\n' "$src"
