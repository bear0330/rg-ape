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

The reviewed contract is [rg.apebind.yaml](rg.apebind.yaml). `search` also
accepts `ignore_case`, `files_with_matches`, `json`, `glob`, `hidden`, and
`no_ignore`. Python uses those `snake_case` names. Node.js and Java use
`ignoreCase`, `filesWithMatches`, `lineNumber`, and `noIgnore`.

A library call is not a terminal, so `rg` hides line numbers unless that flag
is set. No matches make the binding raise. Each binding's `prepare` script
copies the root `rg.com` into the package before the call above can run from
this tree.

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

Regenerating replaces the generated sources under `bindings/`. The tests live
outside those generated trees.

## Test

```sh
./scripts/test.sh
```

That fetches `rg.com` when `./rg.com` is absent, then checks the binary and
the Python, Node.js, and Java bindings. Java requires a JDK 17 `javac`.

ripgrep's own license is the Unlicense, or the MIT license, at the upstream
project. The binding sources in this repository are MIT.
