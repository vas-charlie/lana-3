# ADR-0013 — Universal search core foundation

- **Status:** U RAZVOJU
- **Date:** 2026-10-11

## Decision

Introduce a platform-neutral universal-search aggregator before connecting concrete data stores.

The core owns only:
- a stable set of search domains from MASTER PLAN unit 25;
- a provider boundary for each domain;
- query validation;
- deterministic aggregation and ordering.

Each data domain remains responsible for its own lookup implementation. The aggregator never fabricates a result for a domain that has no provider.

## Initial domains

- notes
- journal
- clients
- rides
- business data
- documents

## Safety and architecture rules

- Blank queries fail explicitly.
- A domain can have only one registered provider in one aggregator instance, preventing ambiguous ownership.
- Provider results retain their source domain.
- Missing providers produce no guessed results.
- The shared core has no Android, database, cloud, vendor, or language dependency.

## Not claimed by this slice

This does not yet connect Notes, Journal, clients, rides, Business OS, or documents. It does not provide ranking quality, full-text indexing, semantic/vector search, UI, sync, or physical acceptance.

## Next integration gate

Connect one existing source through a dedicated adapter and prove it with integration tests before adding further sources.
