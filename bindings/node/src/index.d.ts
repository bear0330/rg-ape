// Public exports: handwritten semantic API and generated transport utilities.
export {
  createClient,
  type ClientOptions,
  type SearchParameters,
  raw,
} from './client.js';
export { search, SearchClient, type SearchOptions } from './search.js';
export {
  APEEvent,
  APEEventError,
  type APEEventMap,
  APEProcessError,
  APEProcessResult,
  APETimeoutError,
  ProcessSession,
  type RawOptions,
} from './_runtime.js';
