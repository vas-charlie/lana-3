extends Node3D
## LANA production avatar scene adapter.
## Expects a separately authored, licensed rigged GLB with named blend shapes.
## Does NOT fabricate a human likeness or lip-sync from text.
##
## Attach to the scene root; add the model as a child of ModelRoot.
## The Android host may send normalized channel weights through a Godot plugin.

@export var model_path: String = "res://assets/lana_rigged.glb"
@onready var model_root: Node3D = $ModelRoot

var _model: Node3D
var _meshes: Array[MeshInstance3D] = []
var _ready_for_pose := false

# Minimum independently controllable features before claiming rig readiness.
const REQUIRED_MORPHS := [
    "eyeBlinkLeft", "eyeBlinkRight", "jawOpen", "mouthClose",
    "mouthPucker", "mouthStretchLeft", "browInnerUp"
]

const MORPH_NAMES := {
    "EYE_BLINK_LEFT": "eyeBlinkLeft",
    "EYE_BLINK_RIGHT": "eyeBlinkRight",
    "BROW_RAISE_LEFT": "browInnerUp",
    "BROW_RAISE_RIGHT": "browInnerUp",
    "JAW_OPEN": "jawOpen",
    "LIPS_CLOSED": "mouthClose",
    "LIPS_WIDE": "mouthStretchLeft",
    "LIPS_ROUNDED": "mouthPucker",
    "LIPS_LOWER_BITE": "mouthRollLower",
    "SMILE_LEFT": "mouthSmileLeft",
    "SMILE_RIGHT": "mouthSmileRight"
}

func _ready() -> void:
    var packed := load(model_path) as PackedScene
    if packed == null:
        push_warning("LANA: no rigged model packaged; production avatar unavailable")
        return
    _model = packed.instantiate() as Node3D
    if _model == null:
        push_error("LANA: avatar asset is not a Node3D scene")
        return
    model_root.add_child(_model)
    _collect_meshes(_model)
    _ready_for_pose = _has_required_rig_channels()
    if not _ready_for_pose:
        push_warning("LANA: rig missing required independent facial blend shapes; avatar unavailable")

func _has_required_rig_channels() -> bool:
    var available := {}
    for mesh_instance in _meshes:
        if mesh_instance.mesh == null:
            continue
        for index in mesh_instance.mesh.get_blend_shape_count():
            available[String(mesh_instance.mesh.get_blend_shape_name(index))] = true
    for morph_name in REQUIRED_MORPHS:
        if not available.has(morph_name):
            return false
    return true

func _collect_meshes(node: Node) -> void:
    if node is MeshInstance3D:
        _meshes.append(node as MeshInstance3D)
    for child in node.get_children():
        _collect_meshes(child)

func is_production_model_ready() -> bool:
    return _ready_for_pose

## Android bridge calls this with a map of AvatarRigChannel names to weights.
## Unsupported channels are ignored, never substituted with portrait motion.
func apply_rig_pose(channels: Dictionary) -> void:
    if not _ready_for_pose:
        return
    for channel in channels:
        var value := clampf(float(channels[channel]), -1.0, 1.0)
        if channel == "HEAD_YAW":
            _model.rotation.y = value * deg_to_rad(18.0)
        elif channel == "HEAD_PITCH":
            _model.rotation.x = value * deg_to_rad(12.0)
        elif channel == "HEAD_ROLL":
            _model.rotation.z = value * deg_to_rad(8.0)
        elif MORPH_NAMES.has(channel):
            _set_morph(MORPH_NAMES[channel], maxf(0.0, value))

func _set_morph(morph_name: String, weight: float) -> void:
    for mesh_instance in _meshes:
        if mesh_instance.mesh == null:
            continue
        for index in mesh_instance.mesh.get_blend_shape_count():
            if String(mesh_instance.mesh.get_blend_shape_name(index)) == morph_name:
                mesh_instance.set_blend_shape_value(index, weight)
