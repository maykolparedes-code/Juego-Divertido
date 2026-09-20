class_name CharacterBuilder
extends RefCounted

## Construye un personaje "blocky" low-poly por código (cajas de colores),
## sin depender de ningún modelo 3D externo. Devuelve un Node3D "Visuals"
## con nodos hijos nombrados (Torso, Head, LeftArmPivot, RightArmPivot,
## LeftLegPivot, RightLegPivot) para que player.gd/npc.gd puedan animarlos
## rotando los pivotes de las extremidades.
##
## Cuando quieras subir la calidad visual, puedes reemplazar esto por un
## modelo .glb importado (por ejemplo, un personaje animado de Mixamo) sin
## tocar la lógica de juego: solo tendrías que adaptar player.gd/npc.gd para
## usar un AnimationPlayer/AnimationTree del modelo en vez de estos pivotes.

static func build(body_color: Color, limb_color: Color) -> Node3D:
	var visuals := Node3D.new()
	visuals.name = "Visuals"

	var torso := _make_box(Vector3(0.5, 0.6, 0.3), body_color)
	torso.name = "Torso"
	torso.position = Vector3(0, 1.05, 0)
	visuals.add_child(torso)

	var head := _make_box(Vector3(0.4, 0.4, 0.4), body_color)
	head.name = "Head"
	head.position = Vector3(0, 1.6, 0)
	visuals.add_child(head)

	var left_arm := _make_limb_pivot(Vector3(-0.35, 1.35, 0), Vector3(0.15, 0.5, 0.15), limb_color)
	left_arm.name = "LeftArmPivot"
	visuals.add_child(left_arm)

	var right_arm := _make_limb_pivot(Vector3(0.35, 1.35, 0), Vector3(0.15, 0.5, 0.15), limb_color)
	right_arm.name = "RightArmPivot"
	visuals.add_child(right_arm)

	var left_leg := _make_limb_pivot(Vector3(-0.15, 0.75, 0), Vector3(0.18, 0.6, 0.18), limb_color)
	left_leg.name = "LeftLegPivot"
	visuals.add_child(left_leg)

	var right_leg := _make_limb_pivot(Vector3(0.15, 0.75, 0), Vector3(0.18, 0.6, 0.18), limb_color)
	right_leg.name = "RightLegPivot"
	visuals.add_child(right_leg)

	return visuals

static func _make_box(size: Vector3, color: Color) -> MeshInstance3D:
	var mesh_instance := MeshInstance3D.new()
	var box := BoxMesh.new()
	box.size = size
	mesh_instance.mesh = box
	var material := StandardMaterial3D.new()
	material.albedo_color = color
	mesh_instance.material_override = material
	return mesh_instance

## Un "pivote" es un Node3D vacío colocado en la articulación (hombro/cadera);
## la caja de la extremidad se desplaza hacia abajo dentro de ese pivote, así
## rotar el pivote balancea la extremidad como un péndulo alrededor de la
## articulación en vez de alrededor de su propio centro.
static func _make_limb_pivot(joint_position: Vector3, limb_size: Vector3, color: Color) -> Node3D:
	var pivot := Node3D.new()
	pivot.position = joint_position
	var limb := _make_box(limb_size, color)
	limb.name = "Mesh"
	limb.position = Vector3(0, -limb_size.y / 2.0, 0)
	pivot.add_child(limb)
	return pivot
