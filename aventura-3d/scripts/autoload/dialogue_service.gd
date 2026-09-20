extends Node

## Autoload mediador: desacopla quién PIDE un diálogo (un NPC en el mundo 3D)
## de quién lo MUESTRA (la UI DialogueBox, en un CanvasLayer aparte). La UI se
## registra a sí misma en su _ready(); cualquier NPC puede pedir un diálogo
## sin conocer la ruta de la escena de UI.

var _ui: Node = null

func register_ui(ui: Node) -> void:
	_ui = ui

func start_dialogue(speaker: String, lines: Array[String], on_finished: Callable = Callable()) -> void:
	if lines.is_empty():
		if on_finished.is_valid():
			on_finished.call()
		return
	if _ui == null:
		push_warning("DialogueService: no hay UI de diálogo registrada todavía.")
		if on_finished.is_valid():
			on_finished.call()
		return
	_ui.show_dialogue(speaker, lines, on_finished)
