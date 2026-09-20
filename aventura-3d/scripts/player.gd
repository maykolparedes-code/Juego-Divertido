extends CharacterBody3D

const MOVE_SPEED := 4.5
const JUMP_VELOCITY := 6.0
const ROTATION_SPEED := 10.0
const CAMERA_YAW_SPEED := 0.006
const CAMERA_PITCH_SPEED := 0.006
const CAMERA_PITCH_MIN := -1.05  # ~-60 grados
const CAMERA_PITCH_MAX := 0.17   # ~10 grados
const RESPAWN_Y := -15.0
const WALK_ANIM_SPEED := 8.0
const IDLE_ANIM_SPEED := 2.0
const LIMB_RETURN_SPEED := 5.0

@onready var camera_pivot: Node3D = $CameraPivot
@onready var spring_arm: SpringArm3D = $CameraPivot/SpringArm3D

var visuals: Node3D
var _left_arm: Node3D
var _right_arm: Node3D
var _left_leg: Node3D
var _right_leg: Node3D

var _touch_controls: Node = null
var _gravity: float = ProjectSettings.get_setting("physics/3d/default_gravity")
var _walk_time := 0.0
var _idle_time := 0.0
var _spawn_position: Vector3

func _ready() -> void:
	add_to_group("player")
	_spawn_position = global_position

	visuals = CharacterBuilder.build(Color(0.20, 0.55, 0.95), Color(0.15, 0.35, 0.75))
	add_child(visuals)
	_left_arm = visuals.get_node("LeftArmPivot")
	_right_arm = visuals.get_node("RightArmPivot")
	_left_leg = visuals.get_node("LeftLegPivot")
	_right_leg = visuals.get_node("RightLegPivot")

	_touch_controls = get_tree().get_first_node_in_group("touch_controls")
	if _touch_controls:
		_touch_controls.look_delta.connect(_on_look_delta)
		_touch_controls.jump_pressed.connect(_on_jump_pressed)

func _physics_process(delta: float) -> void:
	if global_position.y < RESPAWN_Y:
		_respawn()
		return

	if not is_on_floor():
		velocity.y -= _gravity * delta

	var input_vector := Input.get_vector("move_left", "move_right", "move_forward", "move_back")
	if input_vector == Vector2.ZERO and _touch_controls:
		input_vector = _touch_controls.get_move_vector()

	var direction := Vector3.ZERO
	if input_vector.length() > 0.05:
		var cam_basis: Basis = camera_pivot.global_transform.basis
		var forward := -cam_basis.z
		forward.y = 0.0
		forward = forward.normalized()
		var right := cam_basis.x
		right.y = 0.0
		right = right.normalized()
		direction = (right * input_vector.x + forward * -input_vector.y).normalized()

	var is_moving := direction.length() > 0.05
	if is_moving:
		velocity.x = direction.x * MOVE_SPEED
		velocity.z = direction.z * MOVE_SPEED
		var target_angle := atan2(direction.x, direction.z)
		visuals.rotation.y = lerp_angle(visuals.rotation.y, target_angle, ROTATION_SPEED * delta)
	else:
		velocity.x = move_toward(velocity.x, 0.0, MOVE_SPEED * delta * 4.0)
		velocity.z = move_toward(velocity.z, 0.0, MOVE_SPEED * delta * 4.0)

	if Input.is_action_just_pressed("jump") and is_on_floor():
		velocity.y = JUMP_VELOCITY

	move_and_slide()
	_animate(delta, is_moving)

func _on_jump_pressed() -> void:
	if is_on_floor():
		velocity.y = JUMP_VELOCITY

func _on_look_delta(delta: Vector2) -> void:
	camera_pivot.rotation.y -= delta.x * CAMERA_YAW_SPEED
	spring_arm.rotation.x = clamp(spring_arm.rotation.x - delta.y * CAMERA_PITCH_SPEED, CAMERA_PITCH_MIN, CAMERA_PITCH_MAX)

func _animate(delta: float, is_moving: bool) -> void:
	if is_moving:
		_walk_time += delta * WALK_ANIM_SPEED
		var swing := sin(_walk_time) * 0.6
		_left_arm.rotation.x = swing
		_right_arm.rotation.x = -swing
		_left_leg.rotation.x = -swing
		_right_leg.rotation.x = swing
		visuals.position.y = absf(sin(_walk_time)) * 0.03
	else:
		_idle_time += delta * IDLE_ANIM_SPEED
		visuals.position.y = sin(_idle_time) * 0.02
		_left_arm.rotation.x = lerp(_left_arm.rotation.x, 0.0, delta * LIMB_RETURN_SPEED)
		_right_arm.rotation.x = lerp(_right_arm.rotation.x, 0.0, delta * LIMB_RETURN_SPEED)
		_left_leg.rotation.x = lerp(_left_leg.rotation.x, 0.0, delta * LIMB_RETURN_SPEED)
		_right_leg.rotation.x = lerp(_right_leg.rotation.x, 0.0, delta * LIMB_RETURN_SPEED)

func _respawn() -> void:
	global_position = _spawn_position
	velocity = Vector3.ZERO
