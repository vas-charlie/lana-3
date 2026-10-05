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

The final avatar asset and renderer are therefore intentionally separate from the current Android state engine.
