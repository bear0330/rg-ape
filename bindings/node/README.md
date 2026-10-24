# rg-ape Node.js binding

`search` runs the bundled `rg.com` and returns the matching text. The package
has no runtime npm dependencies.

```js
import { search } from 'rg-ape';

console.log(await search({
  pattern: 'ape',
  path: 'notes',
  lineNumber: true,
}));
console.log(await search({
  pattern: 'a.c',
  path: 'notes',
  fixedStrings: true,
  lineNumber: true,
}));
console.log(await search(['-n', '-F', 'a.c', 'notes']));
console.log(await search({ raw: ['-n', '-F', 'a.c', 'notes'] }));
```

Pass a parameter object or raw `rg` argv. One call cannot pass both; that
raises `TypeError` before `rg` runs. An explicit false flag still counts, so
`search({ raw: ['ape'], lineNumber: false })` raises too.

Parameter names are `ignoreCase`, `lineNumber`, `filesWithMatches`, `json`,
`glob`, `hidden`, `noIgnore`, `pcre2`, `fixedStrings`, `wordRegexp`,
`context`, `afterContext`, `beforeContext`, `fileType`, and `invertMatch`.
No matches make the binding raise. `raw()` returns the process result. A
raw-style `search` returns the same stripped text as the named call.

`apebind generate` restores the short generated copy of this file and the
generated `search` export in `src/index.js` and `src/index.d.ts`. Point
`search` back at `search_call` and put this usage back before committing.

## WSL2

If an operation fails with `TLSError([0x6300])`, WSL may be routing the bundled
`.com` APE through Windows interop. Run the following in WSL, then restart the
WSL session:

```bash
sudo sh -c 'echo -1 > /proc/sys/fs/binfmt_misc/WSLInterop'
```
