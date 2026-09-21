extends Node

## Lista ordenada de niveles del juego. go_to_level() la usan los portales
## (VictoryZone) para pasar al siguiente, reseteando el progreso de la
## misión (GameState) que es específico de cada nivel.

const LEVELS: Array[String] = [
	"res://scenes/Level1.tscn",
	"res://scenes/Level2.tscn",
]

func go_to_level(level_path: String) -> void:
	GameState.start_new_level(LEVELS.find(level_path))
	get_tree().change_scene_to_file(level_path)

func level_index_of(level_path: String) -> int:
	return LEVELS.find(level_path)
