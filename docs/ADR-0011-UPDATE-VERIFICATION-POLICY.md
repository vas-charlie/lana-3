# ADR-0011 — Downloaded Android update verification policy

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `security/update-verification-policy`

## Decision

The security decision for whether a downloaded APK is an acceptable forward update is a deterministic shared-core policy.

Android extracts package metadata and signing-certificate SHA-256 digests, then passes only normalized values to `UpdateVerificationPolicy`.

A downloaded APK is accepted only when all of these are true:

- package name exactly matches the installed LANA package;
- archive version exactly matches the version advertised by the release;
- archive version is strictly newer than the installed version;
- the installed package has a readable signing identity;
- the downloaded archive has a readable signing identity;
- the complete signer sets are exactly equal.

Any failed check rejects the update.

## Why

Package parsing is Android-specific, but the accept/reject rules are security policy and should be independently testable.

Exact signer-set equality deliberately rejects:

- different signing keys;
- unsigned/unreadable signing identity;
- an archive with an unexpected additional signer;
- downgrade/same-version replacement;
- package-name substitution;
- an APK whose embedded version does not match the release being installed.

## Limits

This policy does not decide:

- whether the release endpoint itself is trustworthy;
- whether Android permits installation from this source;
- whether a download completed successfully;
- whether the user approves the final Android installer prompt.

Those remain separate platform and user-consent gates.

## Acceptance

- [x] shared-core tests cover successful verification and every rejection category;
- [ ] Android lint/build passes with the policy integrated;
- [ ] ephemeral signed release smoke test remains green;
- [ ] real signed updater is physically tested after private signing setup.
