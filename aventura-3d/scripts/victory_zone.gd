extends Area3D

## Punto de llegada al final de un nivel. La primera vez que el jugador
## entra (con la misión de este nivel ya completada) muestra un mensaje de
## cierre y, si [next_level_path] no está vacío, pasa al siguiente nivel al
## terminar el diálogo. Si está vacío, este es el final del juego (por
## ahora).

@export var next_level_path: String = ""
@export var completion_lines: Array[String] = [
	"¡Encontraste el tesoro del templo!",
]

var _triggered := false
var _spin_time := 0.0

func _ready() -> void:
	body_entered.connect(_on_body_entered)

func _process(delta: float) -> void:
	_spin_time += delta
	var mesh: MeshInstance3D = get_node_or_null("MeshInstance3D")
	if mesh:
		mesh.rotate_y(delta * 1.0)
		mesh.position.y = 1.0 + sin(_spin_time) * 0.15

func _on_body_entered(body: Node3D) -> void:
	if _triggered:
		return
	if not body.is_in_group("player"):
		return
	if GameState.quest_stage != GameState.QuestStage.COMPLETE:
		return
	_triggered = true
	DialogueService.start_dialogue("", completion_lines, _on_dialogue_finished)

func _on_dialogue_finished() -> void:
	if next_level_path != "":
		LevelManager.go_to_level(next_level_path)
