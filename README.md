# rg-ape

ripgrep 15.2.0 as a portable CLI, and from Python, Node.js, or Java.

`rg-ape` takes a finished Cosmopolitan `rg.com` and exposes the same search
through language-native bindings generated with
[APEBind](https://github.com/nuwainfo/apebind). This repository does not build
`rg.com`.

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
import java.nio.file.Path;

String text = RgAPEBinding.search(
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
RgAPEBinding.search(List.of("-n", "-F", "a.c", "notes"));
```

An explicit false flag still counts as a named parameter, so
`search(raw=["ape"], line_number=False)` raises too. Java does the same when
`lineNumber(false)` is combined with `raw` on the builder.

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

## Build `rg.com`

`rg.com` is produced by [rust-ape](https://github.com/bear0330/rust-ape).
From a rust-ape superconfigure checkout:

```sh
bash ./.github/scripts/collectbuild cli/rg
```

That writes `results/bin/rg.com`. Copy it here as `rg.com`, or publish that
same file as the `rg.com` asset on a GitHub release. This repository then
uses the finished binary. Regenerate the bindings with the APEBind binary in
[apebind/dist](../apebind/dist/apebind.com):

```sh
../apebind/dist/apebind.com validate rg.apebind.yaml
../apebind/dist/apebind.com generate rg.apebind.yaml \
  --ape rg.com --lang python -o bindings/python
../apebind/dist/apebind.com generate rg.apebind.yaml \
  --ape rg.com --lang node -o bindings/node
../apebind/dist/apebind.com generate rg.apebind.yaml \
  --ape rg.com --lang java -o bindings/java
```

Regenerating replaces the generated sources under `bindings/`, including
`bindings/python/README.md`, `bindings/node/README.md`, and
`bindings/java/README.md`. It also resets `pyproject.toml`, `package.json`,
and `pom.xml` to version 0.1.0, and restores the generated `search` export in
`bindings/python/src/rg_ape/__init__.py`, `bindings/node/src/index.js`,
`bindings/node/src/index.d.ts`, and `RgAPEBinding.java`. Point Python and
Node `search` back at `search_call`, reapply the Java raw overload plus the
builder guard, and put the binding README usage back, before committing. The
tests live outside those generated trees.

## Test

```sh
./scripts/test.sh
```

That fetches `rg.com` when `./rg.com` is absent, then checks the binary and
the Python, Node.js, and Java bindings. Java requires a JDK 17 `javac`.

ripgrep's own license is the Unlicense, or the MIT license, at the upstream
project. The binding sources in this repository are MIT.
