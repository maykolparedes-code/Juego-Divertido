extends StaticBody3D

## Aplica una textura moteada tipo piedra al MeshInstance3D hijo.

@export var base_color: Color = Color(0.52, 0.51, 0.5)
@export var variation_color: Color = Color(0.4, 0.39, 0.38)
@export var seed_value: int = 3

func _ready() -> void:
	var mesh_instance: MeshInstance3D = $MeshInstance3D
	mesh_instance.material_override = TextureFactory.mottled_material(
		base_color, variation_color, 2.0, 0.95, 5, seed_value
	)
