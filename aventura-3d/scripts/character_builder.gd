class_name CharacterBuilder
extends RefCounted

## Construye personajes low-poly "de cápsulas" por código (sin depender de
## ningún modelo 3D externo). Devuelve un Node3D "Visuals" con nodos hijos
## nombrados (Torso, Head, LeftArmPivot, RightArmPivot, LeftLegPivot,
## RightLegPivot) para que player.gd/npc.gd puedan animarlos rotando los
## pivotes de las extremidades.
##
## build() da un personaje genérico (usado por el NPC). build_prince() da
## el personaje del jugador: túnica con textura, capa, corona, espada y
## escudo, sobre la misma base animable.
##
## Cuando quieras subir la calidad visual más allá de esto, puedes
## reemplazar el resultado por un modelo .glb importado (por ejemplo, un
## personaje animado de Mixamo) sin tocar la lógica de juego: solo
## adaptarías player.gd/npc.gd para usar el AnimationPlayer/AnimationTree
## del modelo en vez de estos pivotes.

static func build(body_color: Color, limb_color: Color) -> Node3D:
	var visuals := _build_base(_make_material(body_color), _make_material(limb_color))
	return visuals

static func build_prince() -> Node3D:
	var skin_material := _make_material(Color(0.93, 0.76, 0.62))
	var tunic_material := TextureFactory.mottled_material(
		Color(0.16, 0.22, 0.55), Color(0.1, 0.15, 0.4), 2.0, 0.5, 4, 42
	)
	tunic_material.rim_enabled = true
	tunic_material.rim = 0.3
	tunic_material.rim_tint = 0.5
	var trouser_material := _make_material(Color(0.22, 0.18, 0.32))

	var visuals := _build_base(skin_material, skin_material, tunic_material, trouser_material)

	var gold_material := _make_material(Color(0.85, 0.68, 0.2))
	gold_material.metallic = 0.6
	gold_material.roughness = 0.3

	_add_crown(visuals, gold_material)
	_add_belt(visuals, gold_material)
	_add_cape(visuals)
	_add_sword(visuals.get_node("RightArmPivot"), gold_material)
	_add_shield(visuals.get_node("LeftArmPivot"), gold_material)

	return visuals

static func _build_base(
	body_material: StandardMaterial3D,
	limb_material: StandardMaterial3D,
	torso_material: StandardMaterial3D = null,
	leg_material: StandardMaterial3D = null
) -> Node3D:
	var visuals := Node3D.new()
	visuals.name = "Visuals"

	var torso := _make_capsule(0.24, 0.65, torso_material if torso_material else body_material)
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

	var used_leg_material := leg_material if leg_material else limb_material
	var left_leg := _make_limb_pivot(Vector3(-0.13, 0.7, 0), 0.1, 0.6, used_leg_material)
	left_leg.name = "LeftLegPivot"
	visuals.add_child(left_leg)

	var right_leg := _make_limb_pivot(Vector3(0.13, 0.7, 0), 0.1, 0.6, used_leg_material)
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

static func _add_crown(visuals: Node3D, gold_material: StandardMaterial3D) -> void:
	var crown := MeshInstance3D.new()
	crown.name = "Crown"
	var torus := TorusMesh.new()
	torus.inner_radius = 0.16
	torus.outer_radius = 0.24
	crown.mesh = torus
	crown.material_override = gold_material
	crown.position = Vector3(0, 1.72, 0)
	visuals.add_child(crown)

	# Cuatro puntas simples sobre el aro (conos) para que se lea como corona
	# y no como un simple anillo.
	for angle_deg in [0, 90, 180, 270]:
		var spike := MeshInstance3D.new()
		var spike_mesh := CylinderMesh.new()
		spike_mesh.top_radius = 0.0
		spike_mesh.bottom_radius = 0.05
		spike_mesh.height = 0.12
		spike.mesh = spike_mesh
		spike.material_override = gold_material
		var rad := deg_to_rad(angle_deg)
		spike.position = Vector3(sin(rad) * 0.2, 1.79, cos(rad) * 0.2)
		visuals.add_child(spike)

static func _add_belt(visuals: Node3D, gold_material: StandardMaterial3D) -> void:
	var belt := MeshInstance3D.new()
	belt.name = "Belt"
	var torus := TorusMesh.new()
	torus.inner_radius = 0.2
	torus.outer_radius = 0.27
	belt.mesh = torus
	belt.material_override = gold_material
	# Un TorusMesh sin rotar ya queda "acostado" en el plano XZ (como una
	# dona horizontal), que es la orientación correcta para rodear la
	# cintura vertical de la cápsula del torso — igual que la corona.
	belt.position = Vector3(0, 0.72, 0)
	visuals.add_child(belt)

static func _add_cape(visuals: Node3D) -> void:
	var cape := MeshInstance3D.new()
	cape.name = "Cape"
	var quad := QuadMesh.new()
	quad.size = Vector2(0.44, 0.85)
	cape.mesh = quad

	var cape_material := StandardMaterial3D.new()
	cape_material.albedo_texture = TextureFactory.vertical_gradient(
		Color(0.55, 0.08, 0.12), Color(0.32, 0.04, 0.08)
	)
	cape_material.roughness = 0.8
	cape_material.cull_mode = BaseMaterial3D.CULL_DISABLED
	cape.material_override = cape_material

	cape.position = Vector3(0, 0.95, 0.16)
	cape.rotation_degrees = Vector3(-12, 180, 0)
	visuals.add_child(cape)

static func _add_sword(hand_pivot: Node3D, gold_material: StandardMaterial3D) -> void:
	var sword := Node3D.new()
	sword.name = "Sword"

	var blade_material := StandardMaterial3D.new()
	blade_material.albedo_color = Color(0.75, 0.78, 0.82)
	blade_material.metallic = 0.85
	blade_material.roughness = 0.2

	var blade := MeshInstance3D.new()
	var blade_mesh := BoxMesh.new()
	blade_mesh.size = Vector3(0.06, 0.5, 0.02)
	blade.mesh = blade_mesh
	blade.material_override = blade_material
	blade.position = Vector3(0, 0.38, 0)
	sword.add_child(blade)

	var tip := MeshInstance3D.new()
	var tip_mesh := CylinderMesh.new()
	tip_mesh.top_radius = 0.0
	tip_mesh.bottom_radius = 0.042
	tip_mesh.height = 0.1
	tip.mesh = tip_mesh
	tip.material_override = blade_material
	tip.position = Vector3(0, 0.68, 0)
	sword.add_child(tip)

	var guard := MeshInstance3D.new()
	var guard_mesh := BoxMesh.new()
	guard_mesh.size = Vector3(0.16, 0.04, 0.04)
	guard.mesh = guard_mesh
	guard.material_override = gold_material
	guard.position = Vector3(0, 0.1, 0)
	sword.add_child(guard)

	var hilt_material := _make_material(Color(0.32, 0.2, 0.1))
	var hilt := MeshInstance3D.new()
	var hilt_mesh := CylinderMesh.new()
	hilt_mesh.top_radius = 0.025
	hilt_mesh.bottom_radius = 0.025
	hilt_mesh.height = 0.16
	hilt.mesh = hilt_mesh
	hilt.material_override = hilt_material
	hilt.position = Vector3(0, 0.02, 0)
	sword.add_child(hilt)

	# Se cuelga a la altura de la mano (extremo inferior del pivote del
	# brazo), con una leve inclinación hacia afuera del cuerpo.
	sword.position = Vector3(0.06, -0.5, 0)
	sword.rotation_degrees = Vector3(0, 0, -8)
	hand_pivot.add_child(sword)

static func _add_shield(hand_pivot: Node3D, gold_material: StandardMaterial3D) -> void:
	var shield_material := StandardMaterial3D.new()
	shield_material.albedo_color = Color(0.35, 0.12, 0.12)
	shield_material.metallic = 0.3
	shield_material.roughness = 0.4

	var shield := MeshInstance3D.new()
	shield.name = "Shield"
	var shield_mesh := CylinderMesh.new()
	shield_mesh.top_radius = 0.18
	shield_mesh.bottom_radius = 0.18
	shield_mesh.height = 0.04
	shield.mesh = shield_mesh
	shield.material_override = shield_material
	shield.rotation_degrees = Vector3(0, 0, 90)
	shield.position = Vector3(-0.08, -0.42, 0)

	var boss := MeshInstance3D.new()
	var boss_mesh := SphereMesh.new()
	boss_mesh.radius = 0.05
	boss_mesh.height = 0.1
	boss.mesh = boss_mesh
	boss.material_override = gold_material
	boss.position = Vector3(0, 0.021, 0)
	shield.add_child(boss)

	hand_pivot.add_child(shield)
