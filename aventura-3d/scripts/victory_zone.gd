extends Area3D

## Zona dentro del templo, detrás de la puerta. La primera vez que el
## jugador entra (con la misión ya completada) muestra un mensaje de cierre
## de esta primera aventura.

var _triggered := false
var _spin_time := 0.0

func _ready() -> void:
	body_entered.connect(_on_body_entered)

func _process(delta: float) -> void:
	_spin_time += delta
	$MeshInstance3D.rotate_y(delta * 1.0)
	$MeshInstance3D.position.y = 1.0 + sin(_spin_time) * 0.15

func _on_body_entered(body: Node3D) -> void:
	if _triggered:
		return
	if not body.is_in_group("player"):
		return
	if GameState.quest_stage != GameState.QuestStage.COMPLETE:
		return
	_triggered = true
	DialogueService.start_dialogue(
		"",
		[
			"¡Encontraste el tesoro del templo!",
			"Esta es la primera aventura de tu juego 3D. ¡Ya puedes seguir construyendo más contenido desde aquí!",
		]
	)
