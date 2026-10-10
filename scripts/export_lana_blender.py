"""Export a REAL authored LANA mesh from Blender to GLB, failing closed.

Usage (inside Blender):
  blender -b lana.blend --python scripts/export_lana_blender.py -- /absolute/output/lana_rigged.glb

This does not generate a face or invent facial deformations.
Model the character in Blender/MPFB first. A missing or empty facial shape key
is an error, not a reason to create a placeholder.
"""
import sys
from pathlib import Path
import bpy

REQUIRED = (
    "eyeBlinkLeft", "eyeBlinkRight", "jawOpen", "mouthClose",
    "mouthPucker", "mouthStretchLeft", "browInnerUp",
    "mouthRollLower", "mouthSmileLeft", "mouthSmileRight",
)

def fail(message):
    raise RuntimeError("LANA export refused: " + message)

def nonzero_shape_key(obj, key_name, tolerance=1e-5):
    keys = obj.data.shape_keys
    if keys is None or key_name not in keys.key_blocks:
        return False
    base = keys.key_blocks.get("Basis") or keys.key_blocks[0]
    key = keys.key_blocks[key_name]
    return any((key.data[i].co - base.data[i].co).length > tolerance
               for i in range(len(obj.data.vertices)))

def main():
    argv = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    if len(argv) != 1:
        fail("pass one output .glb path after --")
    output = Path(argv[0]).resolve()
    if output.suffix.lower() != ".glb":
        fail("output must end with .glb")
    meshes = [obj for obj in bpy.context.scene.objects
              if obj.type == "MESH" and not obj.hide_render]
    if not meshes:
        fail("no visible renderable meshes found")

    # Every required control must exist and deform real vertices somewhere.
    missing = [name for name in REQUIRED
               if not any(nonzero_shape_key(obj, name) for obj in meshes)]
    if missing:
        fail("missing or zero-displacement facial shape keys: " + ", ".join(missing))

    # Avoid accidental export of hidden construction meshes/cameras/lights.
    bpy.ops.object.select_all(action="DESELECT")
    for obj in bpy.context.scene.objects:
        if obj.type in {"MESH", "ARMATURE", "EMPTY"} and not obj.hide_render:
            obj.select_set(True)
    selected = list(bpy.context.selected_objects)
    if not selected:
        fail("nothing selected for export")
    bpy.context.view_layer.objects.active = selected[0]

    output.parent.mkdir(parents=True, exist_ok=True)
    bpy.ops.export_scene.gltf(
        filepath=str(output), export_format="GLB",
        use_selection=True, export_morph=True,
        export_skins=True, export_animations=False,
    )
    if not output.is_file() or output.stat().st_size < 1024:
        fail("GLB export did not produce a plausible file")
    print("LANA Blender export created:", output)
    print("NEXT: python scripts/validate_avatar_glb.py", output)
    print("NOT ACCEPTED: still requires Godot visual inspection and S24 testing.")

if __name__ == "__main__":
    main()
