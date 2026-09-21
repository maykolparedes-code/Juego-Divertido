extends CanvasLayer

## Reúne el joystick de movimiento, el arrastre de cámara y los botones de
## salto/interactuar. player.gd busca este nodo por grupo ("touch_controls")
## y se conecta a sus señales; así el jugador no necesita conocer la
## jerarquía interna de esta escena de UI.

signal look_delta(delta: Vector2)
signal jump_pressed
signal attack_pressed

@onready var joystick: Control = $Joystick
@onready var camera_drag_area: Control = $CameraDragArea
@onready var jump_button: Button = $JumpButton
@onready var attack_button: Button = $AttackButton
@onready var interact_button: Button = $InteractButton

func _ready() -> void:
	add_to_group("touch_controls")
	camera_drag_area.look_delta.connect(func(delta: Vector2) -> void: look_delta.emit(delta))
	jump_button.pressed.connect(func() -> void: jump_pressed.emit())
	attack_button.pressed.connect(func() -> void: attack_pressed.emit())
	interact_button.pressed.connect(_on_interact_pressed)
	interact_button.visible = false
	GameState.nearby_interactable_changed.connect(_on_nearby_interactable_changed)

func get_move_vector() -> Vector2:
	return joystick.get_vector()

func _on_nearby_interactable_changed(interactable: Node) -> void:
	interact_button.visible = interactable != null

func _on_interact_pressed() -> void:
	var interactable: Node = GameState.nearby_interactable
	if interactable and interactable.has_method("interact"):
		interactable.interact()
