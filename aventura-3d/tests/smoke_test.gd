extends Node

## Prueba de humo ejecutable con:
##   godot --headless res://tests/SmokeTest.tscn
## (una escena normal, no un --script MainLoop, para que los autoloads
## como GameState/DialogueService se inicialicen igual que en el juego real)
##
## Instancia Level1.tscn de verdad y simula una partida completa (hablar con
## el NPC, recolectar los 3 cristales, volver, comprobar que la puerta se
## abre) para detectar errores de cableado (rutas de nodos, señales, estado)
## que un simple chequeo de "no crashea al cargar" no detectaría.

var _failures: Array[String] = []

func _ready() -> void:
	print("=== SMOKE TEST: iniciando ===")
	await _run_impl()

func _run_impl() -> void:
	# GameState es un singleton que persiste su guardado en disco (user://).
	# Si una corrida anterior dejó la misión avanzada, hay que resetear el
	# estado para que esta prueba parta siempre de cero.
	GameState.reset_progress()

	var world_scene: PackedScene = load("res://scenes/Level1.tscn")
	var world: Node = world_scene.instantiate()
	get_tree().root.add_child.call_deferred(world)
	await get_tree().process_frame
	await get_tree().process_frame
	await get_tree().process_frame

	var hud: Node = world.get_node_or_null("HUD")
	var gate: Node3D = world.get_node_or_null("Gate")
	var npc: Node = world.get_node_or_null("NPC")
	var dialogue_box: Node = world.get_node_or_null("DialogueBox")
	var player: Node = world.get_node_or_null("Player")
	var touch_controls: Node = world.get_node_or_null("TouchControls")

	_check(hud != null, "El nodo HUD existe en Level1.tscn")
	_check(gate != null, "El nodo Gate existe en Level1.tscn")
	_check(npc != null, "El nodo NPC existe en Level1.tscn")
	_check(dialogue_box != null, "El nodo DialogueBox existe en Level1.tscn")
	_check(player != null, "El nodo Player existe en Level1.tscn")
	_check(touch_controls != null, "El nodo TouchControls existe en Level1.tscn")

	if hud == null or gate == null or npc == null or dialogue_box == null or player == null or touch_controls == null:
		_finish()
		return

	# Regresión: Player aparece antes que TouchControls en Level1.tscn, así que
	# en su primer _ready() el grupo "touch_controls" todavía está vacío. Si
	# la búsqueda de TouchControls solo ocurriera una vez en _ready() (en vez
	# de reintentarse hasta encontrarlo), el jugador quedaría sordo a todo
	# input táctil para siempre: exactamente el bug que se reportó jugando en
	# un dispositivo real.
	await get_tree().physics_frame
	await get_tree().physics_frame
	_check(player.get("_touch_controls") == touch_controls,
		"Player encuentra y se conecta a TouchControls aunque este se declare después en la escena")

	var objective_label: Label = hud.get_node("MarginContainer/VBoxContainer/ObjectiveLabel")
	var crystal_label: Label = hud.get_node("MarginContainer/VBoxContainer/CrystalLabel")
	var continue_button: Button = dialogue_box.get_node("Panel/MarginContainer/VBoxContainer/ContinueButton")
	var gate_collision: CollisionShape3D = gate.get_node("CollisionShape3D")

	_check(objective_label.text.begins_with("Objetivo: Habla"),
		"HUD muestra el objetivo inicial correcto (fue: '%s')" % objective_label.text)

	# 1) Primera conversación con el NPC: debe abrir el diálogo y, al
	#    terminarlo, iniciar la misión (COLLECTING).
	npc.interact()
	await get_tree().process_frame
	_check(dialogue_box.visible, "El diálogo se muestra al interactuar con el NPC")
	_check(get_tree().paused, "El árbol se pausa mientras el diálogo está abierto")

	await _advance_dialogue(dialogue_box, continue_button)

	_check(not dialogue_box.visible, "El diálogo se cierra tras avanzarlo hasta el final")
	_check(not get_tree().paused, "El árbol se reanuda al cerrar el diálogo")
	_check(GameState.quest_stage == GameState.QuestStage.COLLECTING,
		"Tras la primera conversación la misión pasa a COLLECTING (quedó en %d)" % GameState.quest_stage)
	_check(objective_label.text.begins_with("Objetivo: Recolecta"),
		"El HUD actualiza el objetivo a 'recolecta' (fue: '%s')" % objective_label.text)

	# 2) Recolectar los 3 cristales (vía GameState directamente, para no
	#    depender de simular el movimiento físico del jugador).
	GameState.add_crystal()
	GameState.add_crystal()
	GameState.add_crystal()
	await get_tree().process_frame

	_check(GameState.crystals_collected == 3,
		"Se cuentan los 3 cristales recolectados (hay %d)" % GameState.crystals_collected)
	_check(crystal_label.text == "Cristales: 3/3",
		"El HUD muestra 'Cristales: 3/3' (fue: '%s')" % crystal_label.text)
	_check(GameState.quest_stage == GameState.QuestStage.READY_TO_RETURN,
		"Al juntar los 3 cristales la misión pasa a READY_TO_RETURN (quedó en %d)" % GameState.quest_stage)

	# 3) Un cristal "de más" no debería contarse ni romper nada.
	GameState.add_crystal()
	_check(GameState.crystals_collected == 3, "Un cristal de más no se cuenta (sigue en %d)" % GameState.crystals_collected)

	# 4) Segunda conversación con el NPC: debe completar la misión.
	npc.interact()
	await get_tree().process_frame
	await _advance_dialogue(dialogue_box, continue_button)

	_check(GameState.quest_stage == GameState.QuestStage.COMPLETE,
		"Tras la segunda conversación la misión pasa a COMPLETE (quedó en %d)" % GameState.quest_stage)
	_check(objective_label.text.begins_with("¡Misión completada"),
		"El HUD muestra el mensaje de misión completada (fue: '%s')" % objective_label.text)

	# 5) La puerta debe abrirse (moverse hacia arriba) y desactivar su colisión.
	var gate_start_y: float = gate.position.y
	await get_tree().create_timer(1.5).timeout
	_check(gate.position.y > gate_start_y + 2.0,
		"La puerta se mueve hacia arriba al completarse la misión (y pasó de %.2f a %.2f)" % [gate_start_y, gate.position.y])
	_check(gate_collision.disabled, "La colisión de la puerta se desactiva al abrirse")

	# Libera el Nivel 1 antes de cargar el Nivel 2, para que no queden dos
	# jugadores/TouchControls a la vez compitiendo por los mismos grupos.
	world.queue_free()
	await get_tree().process_frame
	await get_tree().process_frame

	await _test_level2()

	_finish()

## Chequeo más liviano del Nivel 2: no repite toda la partida (eso ya lo
## prueba a fondo el Nivel 1 con el mismo código compartido), solo confirma
## que la escena está bien armada y que el ciclo de misión también funciona
## ahí, ya con su propio NPC/diálogo/cristales/puerta.
func _test_level2() -> void:
	GameState.start_new_level(1)

	var level2_scene: PackedScene = load("res://scenes/Level2.tscn")
	var level2: Node = level2_scene.instantiate()
	get_tree().root.add_child.call_deferred(level2)
	await get_tree().process_frame
	await get_tree().process_frame
	await get_tree().process_frame

	var hud: Node = level2.get_node_or_null("HUD")
	var gate: Node3D = level2.get_node_or_null("Gate")
	var npc: Node = level2.get_node_or_null("NPC")
	var dialogue_box: Node = level2.get_node_or_null("DialogueBox")
	var player: Node = level2.get_node_or_null("Player")
	var touch_controls: Node = level2.get_node_or_null("TouchControls")

	_check(hud != null, "El nodo HUD existe en Level2.tscn")
	_check(gate != null, "El nodo Gate existe en Level2.tscn")
	_check(npc != null, "El nodo NPC existe en Level2.tscn")
	_check(dialogue_box != null, "El nodo DialogueBox existe en Level2.tscn")
	_check(player != null, "El nodo Player existe en Level2.tscn")
	_check(touch_controls != null, "El nodo TouchControls existe en Level2.tscn")

	if hud == null or gate == null or npc == null or dialogue_box == null:
		return

	var continue_button: Button = dialogue_box.get_node("Panel/MarginContainer/VBoxContainer/ContinueButton")
	var gate_collision: CollisionShape3D = gate.get_node("CollisionShape3D")

	npc.interact()
	await get_tree().process_frame
	await _advance_dialogue(dialogue_box, continue_button)
	_check(GameState.quest_stage == GameState.QuestStage.COLLECTING,
		"Nivel 2: tras hablar con el Sabio la misión pasa a COLLECTING")

	GameState.add_crystal()
	GameState.add_crystal()
	GameState.add_crystal()
	await get_tree().process_frame
	_check(GameState.quest_stage == GameState.QuestStage.READY_TO_RETURN,
		"Nivel 2: las 3 gemas completan la recolección")

	npc.interact()
	await get_tree().process_frame
	await _advance_dialogue(dialogue_box, continue_button)
	_check(GameState.quest_stage == GameState.QuestStage.COMPLETE,
		"Nivel 2: la segunda conversación completa la misión")

	var gate_start_y: float = gate.position.y
	await get_tree().create_timer(1.5).timeout
	_check(gate.position.y > gate_start_y + 2.0, "Nivel 2: la puerta también se abre al completar la misión")
	_check(gate_collision.disabled, "Nivel 2: la colisión de la puerta se desactiva")

func _advance_dialogue(dialogue_box: Node, continue_button: Button) -> void:
	var guard := 0
	while dialogue_box.visible and guard < 20:
		continue_button.emit_signal("pressed")
		await get_tree().process_frame
		guard += 1
	_check(guard < 20, "El diálogo se cerró antes de 20 pulsaciones de 'Continuar' (posible bucle infinito)")

func _check(condition: bool, description: String) -> void:
	if condition:
		print("  OK  - %s" % description)
	else:
		print("  FALLO - %s" % description)
		_failures.append(description)

func _finish() -> void:
	print("=== SMOKE TEST: %d fallo(s) ===" % _failures.size())
	for failure in _failures:
		print("  - %s" % failure)
	get_tree().quit(1 if not _failures.is_empty() else 0)
