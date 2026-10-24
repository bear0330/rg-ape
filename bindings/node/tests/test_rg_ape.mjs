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

const literals = await mkdtemp(join(tmpdir(), 'rg-ape-'));
await writeFile(join(literals, 'lit.txt'), 'abc\na.c\n');
const fixed = await search({
  pattern: 'a.c',
  path: literals,
  lineNumber: true,
  fixedStrings: true,
});
assert.match(fixed, /lit\.txt:2:a\.c/);
assert.doesNotMatch(fixed, /lit\.txt:1:abc/);

const words = await mkdtemp(join(tmpdir(), 'rg-ape-'));
await writeFile(join(words, 'words.txt'), 'alpha ape\napex\n');
const word = await search({
  pattern: 'ape',
  path: words,
  lineNumber: true,
  wordRegexp: true,
});
assert.match(word, /words\.txt:1:alpha ape/);
assert.doesNotMatch(word, /words\.txt:2:apex/);

const lines = await mkdtemp(join(tmpdir(), 'rg-ape-'));
await writeFile(join(lines, 'note.txt'), 'one\ntwo\nthree\nfour\n');
const after = await search({
  pattern: 'two',
  path: lines,
  lineNumber: true,
  afterContext: 1,
});
const before = await search({
  pattern: 'two',
  path: lines,
  lineNumber: true,
  beforeContext: 1,
});
const both = await search({
  pattern: 'two',
  path: lines,
  lineNumber: true,
  context: 1,
});
assert.match(after, /note\.txt:2:two/);
assert.match(after, /note\.txt-3-three/);
assert.doesNotMatch(after, /note\.txt-1-one/);
assert.match(before, /note\.txt-1-one/);
assert.doesNotMatch(before, /note\.txt-3-three/);
assert.match(both, /note\.txt-1-one/);
assert.match(both, /note\.txt-3-three/);
assert.doesNotMatch(both, /note\.txt-4-four/);

const typed = await mkdtemp(join(tmpdir(), 'rg-ape-'));
await writeFile(join(typed, 'code.py'), 'needle\n');
await writeFile(join(typed, 'code.txt'), 'needle\n');
const pyOnly = await search({
  pattern: 'needle',
  path: typed,
  lineNumber: true,
  fileType: 'py',
});
assert.match(pyOnly, /code\.py:1:needle/);
assert.doesNotMatch(pyOnly, /code\.txt/);

const inverted = await mkdtemp(join(tmpdir(), 'rg-ape-'));
await writeFile(join(inverted, 'note.txt'), 'alpha ape\nbeta\n');
const other = await search({
  pattern: 'ape',
  path: inverted,
  lineNumber: true,
  invertMatch: true,
});
assert.match(other, /note\.txt:2:beta/);
assert.doesNotMatch(other, /alpha ape/);

const rawText = await search(['-n', '-F', 'a.c', literals]);
const rawObject = await search({ raw: ['-n', '-F', 'a.c', literals] });
assert.equal(rawText, fixed);
assert.equal(rawObject, fixed);

await assert.rejects(
  search({ raw: ['missing-token', empty] }),
  APEProcessError,
);
await assert.rejects(
  () => search({ pattern: 'ape', path: directory, raw: ['ape', directory] }),
  TypeError,
);
await assert.rejects(
  () => search({ raw: ['ape', directory], lineNumber: false }),
  TypeError,
);
