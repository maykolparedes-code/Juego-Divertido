extends CanvasLayer

## UI de diálogo. Se registra en el autoload DialogueService para que
## cualquier NPC pueda pedirle que muestre texto sin conocer esta escena.
## Mientras habla, pausa el árbol de escena (get_tree().paused = true) para
## congelar el juego; por eso este nodo tiene process_mode = ALWAYS (si no,
## también se congelaría a sí mismo y nadie podría cerrar el diálogo).

@onready var panel: Panel = $Panel
@onready var speaker_label: Label = $Panel/MarginContainer/VBoxContainer/SpeakerLabel
@onready var text_label: Label = $Panel/MarginContainer/VBoxContainer/TextLabel
@onready var continue_button: Button = $Panel/MarginContainer/VBoxContainer/ContinueButton

var _lines: Array[String] = []
var _index := 0
var _on_finished: Callable

func _ready() -> void:
	visible = false
	continue_button.pressed.connect(_advance)
	DialogueService.register_ui(self)

func show_dialogue(speaker: String, lines: Array[String], on_finished: Callable) -> void:
	_lines = lines
	_index = 0
	_on_finished = on_finished
	speaker_label.text = speaker
	speaker_label.visible = speaker != ""
	visible = true
	get_tree().paused = true
	_show_current_line()

func _show_current_line() -> void:
	text_label.text = _lines[_index]
	continue_button.text = "Continuar" if _index < _lines.size() - 1 else "Cerrar"

func _advance() -> void:
	_index += 1
	if _index >= _lines.size():
		_close()
	else:
		_show_current_line()

func _close() -> void:
	visible = false
	get_tree().paused = false
	var callback := _on_finished
	_on_finished = Callable()
	if callback.is_valid():
		callback.call()
