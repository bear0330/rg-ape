# SPDX-License-Identifier: MIT
"""search() accepts named parameters or raw rg argv, not both."""

from __future__ import annotations

from ._generated import raw as _raw
from ._generated import search as _search

_MISSING = object()


def search(*args, raw=_MISSING, **kwargs):
    """Search with named parameters, or with raw rg argv.

    A list or tuple as the only argument is raw argv. Passing raw argv
    together with a pattern, a path, or any flag raises TypeError.
    """
    arguments = _raw_arguments(args, kwargs, raw)
    if arguments is not None:
        result = _raw(arguments)

        return result.stdout.strip()

    return _search(*args, **kwargs)


def _raw_arguments(args, kwargs, raw):
    if args and _is_argv(args[0]):
        if len(args) > 1 or kwargs or raw is not _MISSING:
            raise TypeError('search accepts parameters or raw argv, not both')

        return _argv(args[0])

    if raw is _MISSING:
        return None

    if args or kwargs:
        raise TypeError('search accepts parameters or raw argv, not both')

    return _argv(raw)


def _argv(arguments):
    if not _is_argv(arguments):
        raise TypeError('raw argv must be a list of arguments')

    return [str(argument) for argument in arguments]


def _is_argv(value):
    return isinstance(value, (list, tuple)) and not isinstance(value, (str, bytes))
