# rg-ape Python binding

`search` runs the bundled `rg.com` and returns the matching text.

```python
from rg_ape import search

print(search("ape", "notes", line_number=True))
print(search("a.c", "notes", fixed_strings=True, line_number=True))
print(search(raw=["-n", "-F", "a.c", "notes"]))
print(search(["-n", "-F", "a.c", "notes"]))
```

Pass named parameters or raw `rg` argv. One call cannot pass both; that raises
`TypeError` before `rg` runs. An explicit false flag still counts, so
`search(raw=["ape"], line_number=False)` raises too.

Named parameters are `ignore_case`, `line_number`, `files_with_matches`,
`json`, `glob`, `hidden`, `no_ignore`, `pcre2`, `fixed_strings`,
`word_regexp`, `context`, `after_context`, `before_context`, `file_type`,
and `invert_match`. No matches make the binding raise. `raw()` returns the
process result. A raw-style `search` returns the same stripped text as the
named call.

`apebind generate` restores the short generated copy of this file. Put this
usage back before committing.

## WSL2

If an operation fails with `TLSError([0x6300])`, WSL may be routing the bundled
`.com` APE through Windows interop. Run the following in WSL, then restart the
WSL session:

```bash
sudo sh -c 'echo -1 > /proc/sys/fs/binfmt_misc/WSLInterop'
```
