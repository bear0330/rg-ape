import { raw as generatedRaw, search as generatedSearch } from './client.js';

const CONFLICT = 'search accepts parameters or raw argv, not both';

export async function search(parameters) {
  if (Array.isArray(parameters)) {
    return textFromRaw(parameters);
  }

  if (isRawCall(parameters)) {
    const named = Object.keys(parameters).filter((key) => key !== 'raw');
    if (named.length > 0) {
      throw new TypeError(CONFLICT);
    }

    return textFromRaw(parameters.raw);
  }

  return generatedSearch(parameters);
}

function isRawCall(parameters) {
  return parameters !== null
    && typeof parameters === 'object'
    && Object.prototype.hasOwnProperty.call(parameters, 'raw');
}

async function textFromRaw(arguments_) {
  if (!Array.isArray(arguments_)) {
    throw new TypeError('raw argv must be a list of arguments');
  }

  const result = await generatedRaw(arguments_);

  return result.stdout.trim();
}
