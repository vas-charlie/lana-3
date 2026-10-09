# LANA 3 Avatar Contract v0.1

The avatar is part of the product interface, not decoration. It renders LANA's actual application state and must never imply a capability or emotion that the system has not selected.

## Visual states

The Android preview currently supports:

- IDLE
- LISTENING
- THINKING
- SPEAKING
- OFFLINE
- ERROR

The future avatar renderer consumes these states instead of owning business logic.

User-facing status text is not part of the avatar semantic contract. Text is rendered by the platform localization layer, while the avatar presenter carries only state and semantic animation cues. This prevents the avatar layer from becoming a second source of translated UI copy.

## Animation channels

The renderer must support independent channels so movements can be combined naturally:

- eyes: blink, gaze target, gaze shift
- head: small turn, tilt, nod
- mouth: speech/lip-sync parameters
- face: neutral, warm smile, focused, concerned
- body: breathing/presence, posture
- hands: restrained conversational gestures
- glasses: on, off, adjust
- attention: user, task, screen, neutral

Large gestures must be contextual and sparse. Idle life should primarily come from micro-movements such as breathing, blinking and subtle gaze/head changes.

The current Android preview implements only a lightweight local IDLE breathing motion. Blink, gaze, head movement and lip-sync remain separate future renderer channels and must not be simulated before the visual asset supports them.

## Semantic events

The application may emit semantic avatar cues such as:

- START_LISTENING
- START_THINKING
- START_SPEAKING
- STOP_SPEAKING
- ENTER_OFFLINE
- SHOW_ERROR
- WARM_SMILE
- SMALL_NOD
- ADJUST_GLASSES

A renderer decides how a cue looks. Core business logic must not manipulate bones, frames or facial vertices.

## Personality and privacy context

Avatar expression may adapt to an explicitly established interaction context. Public/passenger/business contexts remain professional. A private context may permit warmer or playful reactions when appropriate. Absence of a passenger must not be guessed from silence alone.

No avatar behavior may alter authorization, taxi decisions, safety policy, or factual answers.

## Device strategy

Galaxy S24 Ultra and Galaxy Tab A9+ are the first physical Android test devices. They are not architecture requirements. Phone may use a closer portrait composition; tablet may expose more body and surrounding task information.

## Performance and fallback

The application must remain usable if advanced avatar rendering is unavailable. A lightweight fallback must expose the same semantic states. Avatar rendering must not block Safe Stop, navigation, calls, or other higher-priority tasks.

## Technology selection gate

Do not lock LANA 3 to a 2D/3D/avatar vendor until a prototype comparison checks:

- real-time Android performance
- battery/thermal impact
- lip-sync latency and quality
- animation control
- offline capability
- licensing and recurring cost
- portability beyond Samsung/Android
- ability to preserve LANA's visual identity

The final avatar asset and renderer are therefore intentionally separate from the current Android state engine. The local micro-motion controller is a preview-only renderer helper and does not move semantic state or business policy into Android presentation code.


## Charlie acceptance target — living presence v1

The immediate phone avatar target is now explicit:

- Lana is visible immediately on the main screen.
- Composition shows the upper body rather than a face-only crop.
- Visual identity: elegant professional blazer, glasses, pen, and a visible Charlie "C" brooch.
- IDLE remains alive through restrained breathing, occasional blink-like micro-motion, gentle gaze/head shifts, and a glasses-adjustment cue.
- OFFLINE and ERROR do not freeze the avatar; Lana remains subtly alive with a downward/writing-style posture cue.
- Visual state is application-driven. The manual state-cycling button is not part of the normal main-screen interaction.
- Semantic state must still be truthful. LISTENING / THINKING / SPEAKING are not to be faked merely to make the avatar look busy.

The current 2D preview can animate the full portrait and stage. Literal glasses removal/return, true eye-only blinking, gaze redirection, and genuine writing hand motion require dedicated alternate frames or a richer renderer and remain renderer work rather than business-state logic.
