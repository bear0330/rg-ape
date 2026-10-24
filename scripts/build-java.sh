#!/bin/sh
# Compile the generated Java binding and pack rg.com into rg-ape.jar.
set -eu

root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
java_root="$root/bindings/java"
resource="$java_root/src/main/resources/apebind/rg.com"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

if [ ! -f "$resource" ]; then
  printf '%s\n' 'Missing bundled rg.com. Run scripts/prepare.sh first.' >&2
  exit 1
fi

find "$java_root/src/main/java" -name '*.java' > "$work/sources.txt"
javac --release 17 -d "$work/classes" @"$work/sources.txt"
mkdir -p "$work/classes"
cp -a "$java_root/src/main/resources/." "$work/classes/"
jar --create --file "$root/rg-ape.jar" -C "$work/classes" .
printf 'Wrote %s\n' "$root/rg-ape.jar"
