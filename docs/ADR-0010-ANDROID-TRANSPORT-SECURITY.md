# ADR-0010 — Android transport security baseline

- **Status:** IN TEST
- **Date:** 2026-10-06
- **Target branch:** `security/disable-cleartext-traffic`

## Decision

LANA 3 explicitly disables Android cleartext network traffic with
`android:usesCleartextTraffic="false"`.

Network integrations must use encrypted transport. A future integration that
cannot operate over HTTPS/TLS must be treated as an explicit architecture and
security exception rather than silently weakening the whole application.

## Why

The current updater already uses HTTPS. Making the platform policy explicit:

- prevents accidental future HTTP use;
- makes the security expectation visible in the manifest;
- avoids relying only on target-SDK defaults;
- creates a clear review point for future local-network or vehicle integrations.

## Limits

This setting does not replace:

- APK signature verification;
- TLS/server authenticity;
- secure credential storage;
- application-level authorization;
- data minimization before cloud transmission.

Local/device integrations that genuinely require non-Internet transport must be
designed and reviewed separately.

## Acceptance

- [ ] Android lint passes.
- [ ] Android developer preview builds.
- [ ] Ephemeral signed release smoke test passes.
- [ ] Current HTTPS updater path remains physically unverified until device test.
