import { raw as generatedRaw, search as generatedSearch } from './client.js';
import { APEProcessError } from './_runtime.js';


export class SearchClient {
  async _invoke(operation, raiseOnNoMatch, isRaw) {
    try {
      const result = await operation();
      return isRaw ? result.stdout.trim() : result;
    } catch (error) {
      if (
        raiseOnNoMatch
        || !(error instanceof APEProcessError)
        || error.result.returnCode !== 1
      ) {
        throw error;
      }

      return '';
    }
  }

  async search(parameters = {}, options = {}) {
    if (Array.isArray(parameters)) {
      return this._invoke(
        () => generatedRaw(parameters),
        options.raiseOnNoMatch ?? true,
        true,
      );
    }

    const { raiseOnNoMatch = options.raiseOnNoMatch ?? true, ...input } = parameters;

    if (Object.prototype.hasOwnProperty.call(input, 'raw')) {
      if (Object.keys(input).length !== 1) {
        throw new TypeError('search accepts parameters or raw argv, not both');
      }
      if (!Array.isArray(input.raw)) {
        throw new TypeError('raw argv must be a list of arguments');
      }

      return this._invoke(() => generatedRaw(input.raw), raiseOnNoMatch, true);
    }

    return this._invoke(() => generatedSearch(input), raiseOnNoMatch, false);
  }
}


const defaultClient = new SearchClient();
export const search = defaultClient.search.bind(defaultClient);
