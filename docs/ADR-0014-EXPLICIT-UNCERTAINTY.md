# ADR-0014: Explicit uncertainty in the shared core

Status: IN DEVELOPMENT (MASTER PLAN unit 42)

The platform-neutral EvidenceValue<T> contract distinguishes KNOWN, UNCERTAIN and UNKNOWN. A KNOWN value must be present; UNKNOWN must not carry a guessed value. UNCERTAIN may contain a candidate, but needs confirmation. Optional evidence text never upgrades certainty and is not a calibrated confidence score.

This boundary does not authorize external actions, define user-facing confirmation dialogs, change Android packaging or claim physical acceptance. Consumers must still apply the permissions and risk policy independently. Add integration tests before wiring this type to Decision Engine decisions.
