extends CanvasLayer

@onready var crystal_label: Label = $MarginContainer/VBoxContainer/CrystalLabel
@onready var objective_label: Label = $MarginContainer/VBoxContainer/ObjectiveLabel

func _ready() -> void:
	GameState.crystals_changed.connect(_on_crystals_changed)
	GameState.quest_stage_changed.connect(_on_quest_stage_changed)
	_on_crystals_changed(GameState.crystals_collected)
	_on_quest_stage_changed(GameState.quest_stage)

func _on_crystals_changed(count: int) -> void:
	crystal_label.text = "Cristales: %d/%d" % [count, GameState.TOTAL_CRYSTALS]

func _on_quest_stage_changed(stage: int) -> void:
	match stage:
		GameState.QuestStage.NOT_STARTED:
			objective_label.text = "Objetivo: Habla con el aldeano"
		GameState.QuestStage.COLLECTING:
			objective_label.text = "Objetivo: Recolecta los cristales"
		GameState.QuestStage.READY_TO_RETURN:
			objective_label.text = "Objetivo: Vuelve con el aldeano"
		GameState.QuestStage.COMPLETE:
			objective_label.text = "¡Misión completada! Explora el templo"
