extends StaticBody3D

## Puerta del templo: bloquea el paso hasta que la misión se completa
## (GameState.quest_stage == COMPLETE), momento en el que se desliza hacia
## arriba y deja de colisionar.

@export var open_offset: Vector3 = Vector3(0, 4, 0)
@export var open_duration: float = 1.2

@onready var collision_shape: CollisionShape3D = $CollisionShape3D

var _closed_position: Vector3
var _tween: Tween

func _ready() -> void:
	_closed_position = position
	GameState.quest_stage_changed.connect(_on_quest_stage_changed)
	if GameState.quest_stage == GameState.QuestStage.COMPLETE:
		position = _closed_position + open_offset
		collision_shape.disabled = true

func _on_quest_stage_changed(stage: int) -> void:
	if stage == GameState.QuestStage.COMPLETE:
		open()

func open() -> void:
	if _tween:
		_tween.kill()
	collision_shape.disabled = true
	_tween = create_tween()
	_tween.tween_property(self, "position", _closed_position + open_offset, open_duration)\
		.set_trans(Tween.TRANS_CUBIC).set_ease(Tween.EASE_OUT)
