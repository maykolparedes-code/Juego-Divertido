extends Node

## Autoload único: estado global de la partida (cristales, misión) y guardado.
## Cualquier escena puede leerlo/escribirlo como "GameState.algo" porque está
## registrado como autoload en project.godot.

signal crystals_changed(count: int)
signal quest_stage_changed(stage: int)
signal nearby_interactable_changed(interactable: Node)

enum QuestStage { NOT_STARTED, COLLECTING, READY_TO_RETURN, COMPLETE }

const TOTAL_CRYSTALS := 3
const SAVE_PATH := "user://savegame.json"

var crystals_collected: int = 0
var quest_stage: int = QuestStage.NOT_STARTED
var nearby_interactable: Node = null
var current_level_index: int = 0
var highest_level_reached: int = 0

func _ready() -> void:
	load_game()

func add_crystal() -> void:
	if quest_stage != QuestStage.COLLECTING:
		return
	if crystals_collected >= TOTAL_CRYSTALS:
		return
	crystals_collected += 1
	crystals_changed.emit(crystals_collected)
	if crystals_collected >= TOTAL_CRYSTALS:
		set_quest_stage(QuestStage.READY_TO_RETURN)
	save_game()

func set_quest_stage(stage: int) -> void:
	if quest_stage == stage:
		return
	quest_stage = stage
	quest_stage_changed.emit(stage)
	save_game()

func set_nearby_interactable(interactable: Node) -> void:
	nearby_interactable = interactable
	nearby_interactable_changed.emit(interactable)

func clear_nearby_interactable(interactable: Node) -> void:
	if nearby_interactable == interactable:
		nearby_interactable = null
		nearby_interactable_changed.emit(null)

func reset_progress() -> void:
	crystals_collected = 0
	quest_stage = QuestStage.NOT_STARTED
	current_level_index = 0
	highest_level_reached = 0
	save_game()

## Llamado por LevelManager al entrar a un nivel nuevo: la misión (hablar,
## recolectar, volver) es propia de cada nivel y arranca de cero, pero
## highest_level_reached se conserva para saber hasta dónde llegó el jugador.
func start_new_level(level_index: int) -> void:
	crystals_collected = 0
	quest_stage = QuestStage.NOT_STARTED
	current_level_index = level_index
	if level_index > highest_level_reached:
		highest_level_reached = level_index
	quest_stage_changed.emit(quest_stage)
	crystals_changed.emit(crystals_collected)
	save_game()

func save_game() -> void:
	var data := {
		"crystals_collected": crystals_collected,
		"quest_stage": quest_stage,
		"current_level_index": current_level_index,
		"highest_level_reached": highest_level_reached,
	}
	var file := FileAccess.open(SAVE_PATH, FileAccess.WRITE)
	if file:
		file.store_string(JSON.stringify(data))
		file.close()

func load_game() -> void:
	if not FileAccess.file_exists(SAVE_PATH):
		return
	var file := FileAccess.open(SAVE_PATH, FileAccess.READ)
	if not file:
		return
	var text := file.get_as_text()
	file.close()
	var parsed = JSON.parse_string(text)
	if typeof(parsed) == TYPE_DICTIONARY:
		crystals_collected = int(parsed.get("crystals_collected", 0))
		quest_stage = int(parsed.get("quest_stage", QuestStage.NOT_STARTED))
		current_level_index = int(parsed.get("current_level_index", 0))
		highest_level_reached = int(parsed.get("highest_level_reached", 0))
