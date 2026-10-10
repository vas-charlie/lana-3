# LANA 3 — 3D model authoring specification v0.1

Status: U RAZVOJU. This specification defines the first original realistic model candidate. It does not mark the avatar as accepted or production-ready.

## Identity target
- Original adult female digital character, visually inspired by the established Lana reference without requiring an identical copy.
- Realistic human proportions and materials; no cartoon, toy, wax, or flat-photo look.
- Neutral base expression must remain recognizably the same person when facial controls are at zero.
- Subtle expressions are preferred over exaggerated animation.

## Required independent facial controls
The exported GLB must contain real morph targets named:
- eyeBlinkLeft
- eyeBlinkRight
- browInnerUp
- jawOpen
- mouthClose
- mouthPucker
- mouthStretchLeft
- mouthRollLower
- mouthSmileLeft
- mouthSmileRight

Independent left/right eyelids are mandatory so natural blinks and a deliberate wink are possible.

## Head and body
- Head yaw, pitch and roll must be driven independently from facial morphs.
- Eyes must remain visually stable during head motion.
- Body/neck deformation must not distort the face.
- Hair and clothing must not obstruct the mouth or eyelids in the default camera framing.

## Appearance gate
Before Android integration, inspect the model in neutral pose and at minimum these poses:
1. both eyes open;
2. left wink;
3. right wink;
4. natural closed-mouth smile;
5. jaw open;
6. rounded lips;
7. wide lips;
8. brow raise;
9. head yaw/pitch/roll.

Reject the candidate if any pose visibly tears the mesh, collapses lips/eyelids, changes identity, clips eyes/teeth, or depends on moving the whole portrait/face texture.

## Export gate
Target file: `godot/assets/lana_rigged.glb`.

The candidate must pass:
`python scripts/validate_avatar_glb.py godot/assets/lana_rigged.glb`

Passing this structural gate proves only that required morph targets exist as real GLB morph data. It does not prove visual quality, licensing, Godot import quality, Android performance, lip sync, or physical acceptance.

## Performance discipline
Do not optimize away facial controls before measurement. First produce a visually valid candidate, then measure Godot/Android frame time, memory and thermals. Reduce geometry/material cost only from evidence, while protecting identity and facial deformation quality.

## Acceptance boundary
The model remains a candidate until:
- license/original authorship is documented;
- Godot desktop import is visually checked;
- required facial controls are exercised;
- Android integration succeeds;
- S24 Ultra performance/thermal behavior is measured;
- Charlie physically accepts the result.
