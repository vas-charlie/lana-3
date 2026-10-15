# ADR-0014 — Explicit uncertainty core

- **Status:** U RAZVOJU
- **Date:** 2026-10-11
- **MASTER PLAN:** unit 42, "Lana ne pogađa"

## Decision

Add a small platform-neutral value wrapper that makes certainty explicit at module boundaries.

Three states are defined:
- KNOWN: a value is present and may be used as known input;
- UNCERTAIN: a candidate value may exist but requires confirmation;
- UNKNOWN: no value exists and the core forbids attaching a guessed value.

The confirmation requirement is derived from certainty instead of being independently mutable.

## Invariants

- KNOWN cannot contain null.
- UNKNOWN cannot contain a value.
- UNCERTAIN and UNKNOWN require confirmation.
- Evidence is optional metadata; its presence does not silently upgrade certainty.
- No confidence percentage is invented by this abstraction.

## Not claimed by this slice

This does not automatically migrate existing Smart Ride, navigation, voice, memory, or UI data. Each integration must be a separate controlled change with its own tests. It also does not define AI confidence scoring.

## Purpose

The type creates a reusable fail-closed boundary so future modules can distinguish verified data, uncertain candidates, and missing data without substituting a convenient guess.
