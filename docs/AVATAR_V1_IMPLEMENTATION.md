# LANA 3 Avatar v1 implementation

Status: IN PROGRESS. This document must not be interpreted as a completed avatar.

## Product truth

The current flat Lana portrait is a visual reference/fallback, not the real avatar.

The real avatar is built as an independent renderer with separately controllable channels:
- eyes: blink and gaze
- head: turn, tilt and nod
- face: expression
- mouth: visemes / lip-sync
- body: breathing and restrained gestures
- attention: user, task, screen, neutral

## Conversation behavior

- User/Charlie speaks -> LISTENING. Lana's mouth stays closed.
- User speech ends -> THINKING.
- Lana audio starts -> SPEAKING.
- Only Lana's own audio may drive mouth/lip-sync values.
- Lana audio ends -> IDLE.

## Architecture rule

Conversation semantics live in shared code. Android, iOS, web and future device renderers consume the same semantic avatar model. No platform-specific renderer is allowed to redefine who is speaking.

## Definition of done

Avatar v1 is DONE only after:
1. a real independently animated face/avatar asset exists,
2. renderer is integrated into the application,
3. eyes/head/face/mouth are independently controllable,
4. lip-sync follows Lana's actual audio,
5. LISTENING never moves Lana's mouth,
6. it builds successfully,
7. it is installed on a physical target device,
8. Charlie tests and accepts it.

Until all eight are true, status remains IN PROGRESS.
