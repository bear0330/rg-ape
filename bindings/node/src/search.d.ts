import type { SearchParameters } from './client.js';

export interface SearchOptions {
  readonly raiseOnNoMatch?: boolean;
}

export class SearchClient {
  search(parameters: SearchParameters & SearchOptions): Promise<string>;
  search(
    parameters: { readonly raw: readonly unknown[] } & SearchOptions,
  ): Promise<string>;
  search(argv: readonly unknown[], options?: SearchOptions): Promise<string>;
}

export function search(parameters: SearchParameters & SearchOptions): Promise<string>;
export function search(
  parameters: { readonly raw: readonly unknown[] } & SearchOptions,
): Promise<string>;
export function search(argv: readonly unknown[], options?: SearchOptions): Promise<string>;
