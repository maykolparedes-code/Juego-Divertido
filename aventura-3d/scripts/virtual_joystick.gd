extends Control

## Joystick virtual de posición fija (esquina inferior izquierda). Responde a
## toques en dispositivos Android y también a clic+arrastre con el mouse, así
## se puede probar el movimiento dentro del editor de Godot en escritorio.

@export var knob_max_distance: float = 50.0

@onready var background: Control = $Background
@onready var knob: Control = $Background/Knob

var _touch_index: int = -1
var _origin: Vector2 = Vector2.ZERO
var _vector: Vector2 = Vector2.ZERO

func _ready() -> void:
	knob.position = _knob_center()
	mouse_filter = Control.MOUSE_FILTER_STOP

func _knob_center() -> Vector2:
	return background.size / 2.0 - knob.size / 2.0

func _gui_input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed and _touch_index == -1:
			_touch_index = event.index
			_origin = event.position
		elif not event.pressed and event.index == _touch_index:
			_reset()
	elif event is InputEventScreenDrag:
		if event.index == _touch_index:
			_update_vector(event.position)
	elif event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT:
		if event.pressed:
			_touch_index = 0
			_origin = event.position
		elif _touch_index == 0:
			_reset()
	elif event is InputEventMouseMotion:
		if _touch_index == 0:
			_update_vector(event.position)

func _update_vector(current_position: Vector2) -> void:
	var delta := current_position - _origin
	var clamped := delta.limit_length(knob_max_distance)
	knob.position = _knob_center() + clamped
	_vector = clamped / knob_max_distance

func _reset() -> void:
	_touch_index = -1
	_vector = Vector2.ZERO
	knob.position = _knob_center()

func get_vector() -> Vector2:
	return _vector
