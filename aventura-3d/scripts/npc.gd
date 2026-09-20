extends Node3D

## Aldeano estático: da la misión, y luego la recompensa, según el estado
## global de la partida (GameState.quest_stage). El diálogo que muestra
## cambia solo según en qué etapa de la misión está el jugador.

@export var npc_name: String = "Aldeano"
@export var dialogue_intro: Array[String] = [
	"¡Hola, viajero! Un templo antiguo se abrió cerca de aquí, pero la puerta está sellada con magia de cristal.",
	"Si me traes 3 cristales que están esparcidos por la isla, podré abrir la puerta para ti.",
]
@export var dialogue_collecting: Array[String] = [
	"Todavía necesito esos cristales. ¡Sigue explorando la isla!",
]
@export var dialogue_ready: Array[String] = [
	"¡Los tienes todos! Con esto puedo abrir la puerta del templo. ¡Buena suerte ahí dentro!",
]
@export var dialogue_complete: Array[String] = [
	"Gracias por tu ayuda, héroe. El templo es tuyo para explorar.",
]

var _idle_time := 0.0
var visuals: Node3D

func _ready() -> void:
	visuals = CharacterBuilder.build(Color(0.85, 0.55, 0.15), Color(0.6, 0.4, 0.15))
	add_child(visuals)

	$InteractionArea.body_entered.connect(_on_body_entered)
	$InteractionArea.body_exited.connect(_on_body_exited)

func _process(delta: float) -> void:
	_idle_time += delta * 1.5
	visuals.position.y = sin(_idle_time) * 0.02

func _on_body_entered(body: Node3D) -> void:
	if body.is_in_group("player"):
		GameState.set_nearby_interactable(self)

func _on_body_exited(body: Node3D) -> void:
	if body.is_in_group("player"):
		GameState.clear_nearby_interactable(self)

func interact() -> void:
	DialogueService.start_dialogue(npc_name, _current_dialogue(), _on_dialogue_finished)

func _current_dialogue() -> Array[String]:
	match GameState.quest_stage:
		GameState.QuestStage.NOT_STARTED:
			return dialogue_intro
		GameState.QuestStage.COLLECTING:
			return dialogue_collecting
		GameState.QuestStage.READY_TO_RETURN:
			return dialogue_ready
		_:
			return dialogue_complete

func _on_dialogue_finished() -> void:
	match GameState.quest_stage:
		GameState.QuestStage.NOT_STARTED:
			GameState.set_quest_stage(GameState.QuestStage.COLLECTING)
		GameState.QuestStage.READY_TO_RETURN:
			GameState.set_quest_stage(GameState.QuestStage.COMPLETE)
		_:
			pass
