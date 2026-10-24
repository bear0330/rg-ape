# rg-ape Java binding

`search` runs the bundled `rg.com` and returns the matching text. The sources
target Java 17 and have no third-party dependencies.

```java
import apebind.generated.rg_ape.RgAPEBinding;
import java.nio.file.Path;
import java.util.List;

String text = RgAPEBinding.search(
    RgAPEBinding.SearchParameters.builder()
        .pattern("ape")
        .path(Path.of("notes"))
        .lineNumber(true)
        .build());

String literal = RgAPEBinding.search(
    RgAPEBinding.SearchParameters.builder()
        .pattern("a.c")
        .path(Path.of("notes"))
        .fixedStrings(true)
        .lineNumber(true)
        .build());

String raw = RgAPEBinding.search(List.of("-n", "-F", "a.c", "notes"));
```

Pass the builder or raw `rg` argv. One call cannot pass both; `build()` throws
`IllegalArgumentException` before `rg` runs. An explicit false flag still
counts, so `lineNumber(false)` combined with `raw` throws too.

Builder names are `ignoreCase`, `lineNumber`, `filesWithMatches`, `json`,
`glob`, `hidden`, `noIgnore`, `pcre2`, `fixedStrings`, `wordRegexp`,
`context`, `afterContext`, `beforeContext`, `fileType`, and `invertMatch`.
`context`, `afterContext`, and `beforeContext` take a `Long`. No matches make
the binding raise. `raw()` returns the process result. A raw-style `search`
returns the same stripped text as the named call.

`apebind generate` restores the short generated copy of this file and removes
the raw overload and builder guard in `RgAPEBinding.java`. Put both back
before committing.

The APE is bundled under `src/main/resources/apebind/rg.com` and extracted to
a temporary executable file on first use. Build with Maven:

```bash
mvn package
```

## WSL2

If an operation fails with `TLSError([0x6300])`, WSL may be routing the bundled
`.com` APE through Windows interop. Run the following in WSL, then restart the
WSL session:

```bash
sudo sh -c 'echo -1 > /proc/sys/fs/binfmt_misc/WSLInterop'
```
