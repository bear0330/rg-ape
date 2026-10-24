# SPDX-License-Identifier: MIT
"""Semantic search API over APEBind's generated process layer."""

from __future__ import annotations

from ._generated import raw as generated_raw
from ._generated import search as generated_search
from ._runtime import APEProcessError


_MISSING = object()


class SearchClient:
    @staticmethod
    def _is_argv(value):
        return isinstance(value, (list, tuple))

    @classmethod
    def _normalize_argv(cls, arguments):
        if not cls._is_argv(arguments):
            raise TypeError('raw argv must be a list of arguments')

        return [str(argument) for argument in arguments]

    @classmethod
    def _raw_arguments(cls, args, kwargs, raw):
        if args and cls._is_argv(args[0]):
            if len(args) > 1 or kwargs or raw is not _MISSING:
                raise TypeError('search accepts parameters or raw argv, not both')

            return cls._normalize_argv(args[0])

        if raw is _MISSING:
            return None

        if args or kwargs:
            raise TypeError('search accepts parameters or raw argv, not both')

        return cls._normalize_argv(raw)

    @staticmethod
    def _invoke(operation, raise_on_no_match):
        try:
            return operation()
        except APEProcessError as error:
            if raise_on_no_match or error.result.return_code != 1:
                raise

            return ''

    def search(self, *args, raw=_MISSING, raise_on_no_match=True, **kwargs):
        arguments = self._raw_arguments(args, kwargs, raw)
        if arguments is not None:
            return self._invoke(
                lambda: generated_raw(arguments).stdout.strip(),
                raise_on_no_match,
            )

        return self._invoke(
            lambda: generated_search(*args, **kwargs),
            raise_on_no_match,
        )


_default_client = SearchClient()
search = _default_client.search
