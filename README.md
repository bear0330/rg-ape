# rg-ape

ripgrep 15.2.0 in one `rg.com` file, and from Python, Node.js, or Java.

That file is an Actually Portable Executable (APE), built with
[Cosmopolitan](https://github.com/jart/cosmopolitan). One download holds the
x86-64 and ARM64 programs for Linux, macOS, and Windows. Run `rg.com`
directly. The Python, Node.js, and Java packages carry the same file and call
it for you.

`rg-ape` takes that finished file and exposes the same search through
language-native bindings generated with
[APEBind](https://github.com/nuwainfo/apebind). This repository does not build
`rg.com`.

<p align="center">
  <img src="assets/rg-ape-banner.webp" alt="rg-ape: ripgrep as an Actually Portable Executable, with Python, Node.js, and Java bindings" width="960" />
</p>

## Use `rg.com`

The release asset is the finished binary. Download the latest
[`rg.com` release](https://github.com/bear0330/rg-ape/releases), or let
`./scripts/prepare.sh` do it. Set `RG_APE_VERSION` to pin a tag, or
`RG_APE_FORCE_DOWNLOAD=1` to refresh a file already present.

```sh
./rg.com --version
./rg.com -n ape notes
```

No matches make `rg` exit 1.

## Bindings

Install one release asset. The package already contains `rg.com`. Python needs 3.11 or newer, Node.js 18 or newer, and Java a JDK 17.

```sh
python3 -m pip install https://github.com/bear0330/rg-ape/releases/download/v0.2.0/rg_ape-0.2.0-py3-none-any.whl
```

```sh
npm install https://github.com/bear0330/rg-ape/releases/download/v0.2.0/rg-ape-0.2.0.tgz
```

The Node package is ESM. Import it from a `.mjs` file, or set `"type": "module"`.

**Release compatibility:** The v0.2.0 artifacts above predate the semantic
layer refactor and do not include `raise_on_no_match`, `raiseOnNoMatch`, or the
Java `rg_ape.SearchClient`. The examples below describe the current source.
Until a new version is released, use the original v0.2.0 APIs when installing
those release assets.

```sh
curl -fsSL -o rg-ape-0.2.0.jar https://github.com/bear0330/rg-ape/releases/download/v0.2.0/rg-ape-0.2.0.jar
javac --release 17 -cp rg-ape-0.2.0.jar Example.java
java -cp "rg-ape-0.2.0.jar:." Example
```

On Windows the classpath separator is `;`. The Java sample below has to sit in a class with `main`.

Python:

```python
from rg_ape import search

print(search("ape", "notes", line_number=True))
```

Node.js:

```js
import { search } from 'rg-ape';

console.log(await search({
  pattern: 'ape',
  path: 'notes',
  lineNumber: true,
}));
```

Java:

```java
import apebind.generated.rg_ape.RgAPEBinding;
import rg_ape.SearchClient;
import java.nio.file.Path;

String text = new SearchClient().search(
    RgAPEBinding.SearchParameters.builder()
        .pattern("ape")
        .path(Path.of("notes"))
        .lineNumber(true)
        .build());
```

The reviewed contract is [rg.apebind.yaml](rg.apebind.yaml). `search` accepts
either those named parameters or raw `rg` argv. One call cannot pass both;
that raises before `rg` runs.

```python
print(search("a.c", "notes", fixed_strings=True, line_number=True))
print(search(raw=["-n", "-F", "a.c", "notes"]))
print(search(["-n", "-F", "a.c", "notes"]))
```

```js
console.log(await search(['-n', '-F', 'a.c', 'notes']));
console.log(await search({ raw: ['-n', '-F', 'a.c', 'notes'] }));
```

```java
new SearchClient().search(List.of("-n", "-F", "a.c", "notes"));
```

An explicit false flag still counts as a named parameter, so
`search(raw=["ape"], line_number=False)` raises too. Java uses separate
`SearchClient.search(SearchParameters)` and `SearchClient.search(List)`
overloads, so named flags cannot accidentally mix with raw argv.

Named parameters are `ignore_case`, `line_number`, `files_with_matches`,
`json`, `glob`, `hidden`, `no_ignore`, `pcre2`, `fixed_strings`,
`word_regexp`, `context`, `after_context`, `before_context`, `file_type`,
and `invert_match`. Python uses those `snake_case` names. Node.js and Java
use `ignoreCase`, `filesWithMatches`, `lineNumber`, `noIgnore`,
`fixedStrings`, `wordRegexp`, `afterContext`, `beforeContext`, `fileType`,
and `invertMatch`. `pcre2`, `json`, `glob`, and `context` are the same name
in all three. The bundled `rg.com` links PCRE2, so a backreference such as
`(\w+) \1` works when that flag is set.

A library call is not a terminal, so `rg` hides line numbers unless that flag
is set. No matches make the binding raise. `raw()` still returns the process
result. A raw-style `search` returns the same stripped text as the named
call. Each binding's `prepare` script copies the root `rg.com` into the
package before the call above can run from this tree.

## No-match behavior

By default `search` raises when ripgrep exits with code 1 (no matches).
Set `raise_on_no_match=False` in Python or `raiseOnNoMatch: false` in
Node.js to receive an empty string instead. Java accepts a second
`raiseOnNoMatch` boolean argument:

```python
search("missing", "notes", raise_on_no_match=False)
search(raw=["missing", "notes"], raise_on_no_match=False)
```

```js
await search({ pattern: 'missing', path: 'notes', raiseOnNoMatch: false });
await search(['missing', 'notes'], { raiseOnNoMatch: false });
```

```java
new SearchClient().search(
    RgAPEBinding.SearchParameters.builder()
        .pattern("missing")
        .path(Path.of("notes"))
        .build(),
    false);
```

Other nonzero exits (such as an invalid path, exit code 2) still raise.

## Build `rg.com`

`rg.com` is produced by [rust-ape](https://github.com/bear0330/rust-ape).
From a rust-ape superconfigure checkout:

```sh
bash ./.github/scripts/collectbuild cli/rg
```

That writes `results/bin/rg.com`. Copy it here as `rg.com`, or publish that
same file as the `rg.com` asset on a GitHub release. This repository then
uses the finished binary. Regenerate the low-level bindings with APEBind (in `PATH`, or set
`APEBIND_BIN` to the executable):

```sh
./scripts/regenerate.sh
```

This validates [rg.apebind.yaml](rg.apebind.yaml), generates into a temporary
directory, and copies only APEBind-owned runtime and generated client files.
Handwritten semantic APIs, public exports, version metadata, and documentation
are preserved. The separation is documented in [bindings/README.md](bindings/README.md).

## Test

```sh
./scripts/test.sh
```

That fetches `rg.com` when `./rg.com` is absent, then checks the binary and
the Python, Node.js, and Java bindings. Java requires a JDK 17 `javac`.

ripgrep's own license is the Unlicense, or the MIT license, at the upstream
project. The binding sources in this repository are MIT.
