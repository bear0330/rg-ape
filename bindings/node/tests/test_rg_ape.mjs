import assert from 'node:assert/strict';
import { mkdtemp, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

import { APEProcessError, search } from '../src/index.js';

const directory = await mkdtemp(join(tmpdir(), 'rg-ape-'));
await writeFile(join(directory, 'note.txt'), 'alpha ape\nbeta\n');

const text = await search({
  pattern: 'ape',
  path: directory,
  lineNumber: true,
});
assert.match(text, /note\.txt:1:alpha ape/);

const dupDirectory = await mkdtemp(join(tmpdir(), 'rg-ape-'));
await writeFile(join(dupDirectory, 'dup.txt'), 'ape ape\nbeta\n');
const backreference = await search({
  pattern: '(\\w+) \\1',
  path: dupDirectory,
  lineNumber: true,
  pcre2: true,
});
assert.match(backreference, /dup\.txt:1:ape ape/);

const empty = await mkdtemp(join(tmpdir(), 'rg-ape-'));
await writeFile(join(empty, 'note.txt'), 'beta\n');
await assert.rejects(
  search({ pattern: 'ape', path: empty }),
  APEProcessError,
);
