extends StaticBody3D

## Aplica una textura moteada generada por código al MeshInstance3D hijo,
## en vez de depender de un material de color plano declarado en la escena.

@export var base_color: Color = Color(0.35, 0.58, 0.3)
@export var variation_color: Color = Color(0.27, 0.46, 0.24)
@export var uv_scale: float = 12.0
@export var cells: int = 8
@export var roughness: float = 0.9
@export var seed_value: int = 1

func _ready() -> void:
	var mesh_instance: MeshInstance3D = $MeshInstance3D
	mesh_instance.material_override = TextureFactory.mottled_material(
		base_color, variation_color, uv_scale, roughness, cells, seed_value
	)
