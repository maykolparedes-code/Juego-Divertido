class_name CharacterBuilder
extends RefCounted

## Construye un personaje low-poly "de cápsulas" por código (sin depender de
## ningún modelo 3D externo). Devuelve un Node3D "Visuals" con nodos hijos
## nombrados (Torso, Head, LeftArmPivot, RightArmPivot, LeftLegPivot,
## RightLegPivot) para que player.gd/npc.gd puedan animarlos rotando los
## pivotes de las extremidades.
##
## Usa formas redondeadas (cápsulas y esferas) en vez de cajas para un
## estilo más amigable tipo "personaje de cápsulas", con ojos simples para
## darle algo de personalidad sin necesitar texturas.
##
## Cuando quieras subir la calidad visual, puedes reemplazar esto por un
## modelo .glb importado (por ejemplo, un personaje animado de Mixamo) sin
## tocar la lógica de juego: solo tendrías que adaptar player.gd/npc.gd para
## usar un AnimationPlayer/AnimationTree del modelo en vez de estos pivotes.

static func build(body_color: Color, limb_color: Color) -> Node3D:
	var visuals := Node3D.new()
	visuals.name = "Visuals"

	var body_material := _make_material(body_color)
	var limb_material := _make_material(limb_color)

	var torso := _make_capsule(0.24, 0.65, body_material)
	torso.name = "Torso"
	torso.position = Vector3(0, 1.0, 0)
	visuals.add_child(torso)

	var head := _make_head(body_material)
	head.name = "Head"
	head.position = Vector3(0, 1.55, 0)
	visuals.add_child(head)

	var left_arm := _make_limb_pivot(Vector3(-0.32, 1.28, 0), 0.08, 0.5, limb_material)
	left_arm.name = "LeftArmPivot"
	visuals.add_child(left_arm)

	var right_arm := _make_limb_pivot(Vector3(0.32, 1.28, 0), 0.08, 0.5, limb_material)
	right_arm.name = "RightArmPivot"
	visuals.add_child(right_arm)

	var left_leg := _make_limb_pivot(Vector3(-0.13, 0.7, 0), 0.1, 0.6, limb_material)
	left_leg.name = "LeftLegPivot"
	visuals.add_child(left_leg)

	var right_leg := _make_limb_pivot(Vector3(0.13, 0.7, 0), 0.1, 0.6, limb_material)
	right_leg.name = "RightLegPivot"
	visuals.add_child(right_leg)

	return visuals

static func _make_material(color: Color) -> StandardMaterial3D:
	var material := StandardMaterial3D.new()
	material.albedo_color = color
	material.roughness = 0.65
	material.metallic = 0.0
	# Un ligero brillo en el borde ("rim light") ayuda a que las formas
	# simples se lean mejor y se vean menos planas, sin necesitar texturas.
	material.rim_enabled = true
	material.rim = 0.35
	material.rim_tint = 0.4
	return material

static func _make_capsule(radius: float, height: float, material: StandardMaterial3D) -> MeshInstance3D:
	var mesh_instance := MeshInstance3D.new()
	var capsule := CapsuleMesh.new()
	capsule.radius = radius
	capsule.height = height
	mesh_instance.mesh = capsule
	mesh_instance.material_override = material
	return mesh_instance

static func _make_head(body_material: StandardMaterial3D) -> Node3D:
	var head := Node3D.new()

	var sphere_mesh := SphereMesh.new()
	sphere_mesh.radius = 0.24
	sphere_mesh.height = 0.48
	var sphere := MeshInstance3D.new()
	sphere.mesh = sphere_mesh
	sphere.material_override = body_material
	head.add_child(sphere)

	var eye_material := StandardMaterial3D.new()
	eye_material.albedo_color = Color(0.05, 0.05, 0.08)
	eye_material.roughness = 0.3

	var left_eye := _make_eye(eye_material)
	left_eye.position = Vector3(-0.09, 0.02, -0.21)
	head.add_child(left_eye)

	var right_eye := _make_eye(eye_material)
	right_eye.position = Vector3(0.09, 0.02, -0.21)
	head.add_child(right_eye)

	return head

static func _make_eye(material: StandardMaterial3D) -> MeshInstance3D:
	var mesh_instance := MeshInstance3D.new()
	var sphere := SphereMesh.new()
	sphere.radius = 0.035
	sphere.height = 0.07
	mesh_instance.mesh = sphere
	mesh_instance.material_override = material
	return mesh_instance

## Un "pivote" es un Node3D vacío colocado en la articulación (hombro/cadera);
## la cápsula de la extremidad se desplaza hacia abajo dentro de ese pivote,
## así rotar el pivote balancea la extremidad como un péndulo alrededor de
## la articulación en vez de alrededor de su propio centro.
static func _make_limb_pivot(joint_position: Vector3, radius: float, length: float, material: StandardMaterial3D) -> Node3D:
	var pivot := Node3D.new()
	pivot.position = joint_position
	var limb := _make_capsule(radius, length, material)
	limb.name = "Mesh"
	limb.position = Vector3(0, -length / 2.0, 0)
	pivot.add_child(limb)
	return pivot
