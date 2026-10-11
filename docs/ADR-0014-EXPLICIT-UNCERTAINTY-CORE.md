# ADR-0014: Explicit uncertainty core

## Status
Accepted for shared-core foundation.

## Context
MASTER PLAN unit 42 requires Lana to prefer explicit uncertainty over false confidence. The core needs a platform-neutral way to carry a value together with what is actually known about it.

## Decision
Introduce `EvidenceValue<T>` with three semantic certainty states:

- `KNOWN`: a value is present and may be used without an uncertainty confirmation.
- `UNCERTAIN`: a candidate value may be present, but it still requires confirmation where the consuming decision needs certainty.
- `UNKNOWN`: no value is carried. The core must not fill the gap with a guess.

The contract deliberately contains no confidence percentage. A percentage without a calibrated source would create precision that the system does not possess. Evidence is metadata only and never upgrades certainty by itself.

Fail-closed invariants:
- `KNOWN` without a value is rejected.
- `UNKNOWN` with a value is rejected.
- `UNCERTAIN` and `UNKNOWN` report that confirmation is needed.

## Boundaries
This slice does not decide UI wording, action risk policy, confirmation dialogs, model confidence calibration, or physical-device behavior. Those remain separate integration work.

## Verification
Shared-core tests cover all invariants and ensure evidence metadata cannot silently turn uncertainty into knowledge.
