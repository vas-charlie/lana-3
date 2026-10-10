# ADR-0015 — Notes adapter for universal search

- **Status:** U RAZVOJU
- **Date:** 2026-10-11

## Decision

Connect the existing local notes repository to Universal Search through a shared-core adapter.

The adapter:
- owns only the NOTES search domain;
- performs case-insensitive text matching over the repository contract;
- preserves note IDs and update timestamps;
- uses the first text line as the result title and the full note text as preview;
- returns no results for a blank query.

No Android storage implementation leaks into the search core. The existing Android SQLite repository remains behind NoteRepository.

## Acceptance boundary

This slice proves the shared integration contract with unit tests. It does not claim final search UI, indexing performance, cross-domain ranking, semantic search, or physical-device acceptance.
