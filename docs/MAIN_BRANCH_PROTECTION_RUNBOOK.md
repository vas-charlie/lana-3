# LANA 3 — Main branch protection runbook

## Why this is still a manual repository gate

The current `main` branch is not protected. LANA 3 already develops through short-lived branches, pull requests and CI, so the repository should eventually enforce the same discipline at GitHub level instead of relying only on habit.

The connected engineering tool does not have repository-administration permission to change branch protection. This runbook records the intended settings without pretending they are enabled.

## Recommended protection for `main`

Configure the repository rule so that changes reach `main` through pull requests and required checks.

Required pull-request checks:

- `security-baseline`
- `shared-core`
- `android-preview`
- `android-release-smoke`

Recommended repository protections:

- require a pull request before merging;
- require the four checks above to pass;
- require branches to be up to date before merge when GitHub can enforce it reliably;
- block force pushes to `main`;
- block deletion of `main`;
- do not allow bypass merely for convenience;
- keep squash merge as the normal merge method for the controlled-slice workflow.

For a single-owner development phase, do **not** require an external reviewer if that would make Charlie unable to merge his own verified work. Review requirements can be tightened later when collaborators exist.

## What is deliberately not a PR required check

`android-signed-release` is not suitable as a pull-request required check because it only runs on `main` push or manual workflow dispatch and depends on private signing secrets.

Its job is release publication, not PR admission.

## Manual setup path

In GitHub:

`lana-3 → Settings/Postavke → Rules/Pravila or Branches/Podružnice → create a rule for main`

GitHub wording can change. Before saving, verify that the rule targets only `main` and does not accidentally block the repository owner from the current solo workflow.

## Verification after enabling

Create a harmless documentation PR and confirm:

1. direct unsafe merge/push paths are blocked as intended;
2. the four CI checks appear as required;
3. a red required check blocks merge;
4. a green PR can still be squash-merged by Charlie;
5. `android-signed-release` remains a post-merge/manual release job rather than a PR blocker.

## DONE gate

Branch protection is not DONE until the GitHub repository itself reports the rule as active and one real test PR proves the rule works.
