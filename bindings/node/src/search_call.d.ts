import type { SearchParameters } from './client.js';

export function search(parameters: SearchParameters): Promise<string>;
export function search(argv: readonly unknown[]): Promise<string>;
export function search(
  parameters: { readonly raw: readonly unknown[] },
): Promise<string>;
