#!/bin/sh
set -eu

repository_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
target_path="$repository_root/rg.com"
version=${RG_APE_VERSION:-}
force_download=${RG_APE_FORCE_DOWNLOAD:-0}

if [ -f "$target_path" ] && [ "$force_download" != "1" ]; then
  printf 'Using cached %s\n' "$target_path"
  exit 0
fi

if [ -n "$version" ]; then
  download_url="https://github.com/bear0330/rg-ape/releases/download/$version/rg.com"
else
  download_url='https://github.com/bear0330/rg-ape/releases/latest/download/rg.com'
fi

temporary_path="$target_path.download"
printf 'Downloading %s\n' "$download_url"
curl --fail --location --retry 3 --output "$temporary_path" "$download_url"

if [ ! -s "$temporary_path" ]; then
  printf '%s\n' 'Downloaded rg.com is empty' >&2
  exit 1
fi

mv -f "$temporary_path" "$target_path"
chmod +x "$target_path"
