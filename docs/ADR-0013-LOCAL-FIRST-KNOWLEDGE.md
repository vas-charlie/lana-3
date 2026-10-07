# ADR-0013 — Local-first knowledge and AI fallback

## Status

Accepted for the LANA 3 architecture.

## Context

LANA must become more useful through real use while reducing repeated paid AI calls.

The goal is practical rather than academic: if LANA has already learned a reusable, verified answer, rule, procedure or outcome, it should not pay an external AI to rediscover the same thing every time.

At the same time, LANA must not silently treat one model answer as truth, learn mistakes permanently, leak Charlie's private information, or turn uncertain material into executable business rules.

## Decision

LANA uses a local-first knowledge policy.

Resolution order:

1. deterministic local code/rules when the problem is deterministic;
2. verified, fresh local LANA knowledge;
3. an on-device model when technically useful and available;
4. the lowest-cost external reasoning service that is sufficient;
5. a high-capability external AI only when the task actually requires it.

External AI is therefore a teacher/fallback, not LANA's permanent default brain.

### Learning contract

An external AI answer enters LANA knowledge as **CANDIDATE**, never as automatically trusted truth.

A candidate can become **VERIFIED** only through an explicit verifier, for example:

- Charlie confirms it;
- a deterministic computation proves it;
- a trusted document/source proves it;
- an observed real-world outcome proves it;
- another approved verification rule proves it.

Knowledge can also be marked **STALE** or **REJECTED**.

Each durable knowledge item carries at least:

- stable id/key;
- content or normalized rule/result;
- private/generalized scope;
- source kind and optional source reference;
- candidate/verified/stale/rejected status;
- confidence;
- learned/verified timestamps;
- optional expiry/freshness boundary.

### Reuse contract

LANA reuses a knowledge item locally only when it is:

- present;
- VERIFIED;
- not stale or expired;
- above the configured confidence threshold;
- allowed in the current privacy scope.

Otherwise LANA asks for new reasoning or verification instead of guessing.

### Privacy and future commercialization

Private/personal knowledge is never automatically generalized or sold.

If LANA later develops reusable generalized skills or knowledge that could be offered to other users, that is a separate product/legal/privacy decision. Only explicitly generalized, rights-cleared material may enter such a path.

No private Charlie records, conversations, clients, rides, location history or other personal/business data become saleable knowledge by default.

### What this is not

The first implementation does **not** continuously retrain model weights.

Initial LANA learning is durable knowledge, rules, examples, outcomes, retrieval and verification. Model training/fine-tuning may be evaluated later only when technically, legally and financially justified.

## Cost-control effect

The target loop is:

`unknown → use sufficient AI/reasoning → verify useful result → store → reuse locally next time`

As verified local coverage grows, external AI calls should decrease for repeated tasks.

## Initial implementation

Shared core defines:

- `KnowledgeEntry`;
- provenance/scope/status metadata;
- `KnowledgeStore` persistence boundary;
- `KnowledgeReusePolicy`;
- `KnowledgeLearningPolicy`;
- explicit fallback reasons.

Persistence, retrieval/indexing, semantic search and provider routing are subsequent slices.

## Acceptance tests

The shared-core policy must prove that:

1. missing knowledge falls back instead of guessing;
2. verified fresh high-confidence knowledge is reused locally;
3. an external AI answer is not reused merely because it exists;
4. verification can promote a candidate;
5. stale/expired or low-confidence material is rechecked;
6. private knowledge is rejected from a generalized/non-private context.
