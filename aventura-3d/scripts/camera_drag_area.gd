extends Control

## Zona invisible que cubre el lado derecho de la pantalla: arrastrar el dedo
## (o el mouse con el botón derecho, para probar en escritorio) ahí gira la
## cámara. Emite el delta de movimiento en píxeles para que player.gd lo
## traduzca en rotación de cámara.

signal look_delta(delta: Vector2)

var _touch_index: int = -1
var _dragging_with_mouse: bool = false
var _last_position: Vector2 = Vector2.ZERO

func _ready() -> void:
	mouse_filter = Control.MOUSE_FILTER_STOP

func _gui_input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		if event.pressed and _touch_index == -1:
			_touch_index = event.index
			_last_position = event.position
		elif not event.pressed and event.index == _touch_index:
			_touch_index = -1
	elif event is InputEventScreenDrag:
		if event.index == _touch_index:
			look_delta.emit(event.position - _last_position)
			_last_position = event.position
	elif event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_RIGHT:
		_dragging_with_mouse = event.pressed
		_last_position = event.position
	elif event is InputEventMouseMotion:
		if _dragging_with_mouse:
			look_delta.emit(event.position - _last_position)
			_last_position = event.position
