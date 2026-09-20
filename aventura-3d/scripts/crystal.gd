extends Area3D

## Cristal recolectable. Solo cuenta si la misión está activa
## (GameState.quest_stage == COLLECTING); si el jugador lo toca antes de
## hablar con el aldeano, no pasa nada todavía.

var _spin_time := 0.0

func _ready() -> void:
	body_entered.connect(_on_body_entered)

func _process(delta: float) -> void:
	_spin_time += delta
	rotate_y(delta * 1.5)
	position.y = 0.9 + sin(_spin_time * 2.0) * 0.1

func _on_body_entered(body: Node3D) -> void:
	if not body.is_in_group("player"):
		return
	if GameState.quest_stage != GameState.QuestStage.COLLECTING:
		return
	GameState.add_crystal()
	queue_free()
