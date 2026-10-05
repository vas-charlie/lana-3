# LANA Call Assistant

Status: product requirement recorded for later implementation and platform feasibility validation.

## Goal

When Charlie is unavailable, sleeping, driving, or otherwise unable to answer, Lana may act as a transparent digital call assistant and take a message.

## Required behavior

- Lana must identify herself as Lana, Charlie's assistant. She must never impersonate Charlie.
- Example Croatian greeting: "Dobar dan, ja sam Lana, Charliejeva asistentica. Charlie se trenutno ne može javiti. Možete ostaviti poruku, ja ću mu je prenijeti."
- Lana should speak immediately after answering. She must not answer silently and wait for the caller to speak first.
- For a known caller, use the stored preferred language when available.
- For an unknown caller, the country calling code may be used only as an initial language hint, never as proof of language.
- After the caller responds, detected spoken language may confirm or correct the initial language and Lana should switch naturally when needed.
- The system must support languages through the general multilingual architecture rather than a fixed hardcoded language list.
- Lana records a structured message for Charlie: caller identity/number when available, time, detected language, message, and actionable details.
- Initial authorization is message-taking only. Lana must not promise a taxi reservation, price, appointment, or other commitment unless Charlie has explicitly granted that capability and authorization.
- Unknown or uncertain caller/language/context must remain explicitly uncertain rather than guessed.

## Taxi integration

A caller's message may later be parsed into a draft taxi reservation containing details such as name, date/time, passenger count, pickup and destination. A draft is not a confirmed reservation.

## Platform constraint

Automatic answering and two-way audio for ordinary carrier calls must be verified against the current Android/iOS/device APIs and permissions before implementation. Existing Samsung behavior is a useful product reference, not an assumption that a third-party LANA 3 application receives the same system privileges.

## Related modules

Phone/communication, speech input/output, language context, background behavior, driving context, authorization, reservations, notifications, and diagnostics.
