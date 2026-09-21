extends StaticBody3D

## Aplica texturas moteadas (corteza en el tronco, follaje en las copas) por
## código, en vez de materiales de color plano.

@export var trunk_color: Color = Color(0.38, 0.24, 0.14)
@export var trunk_variation: Color = Color(0.26, 0.16, 0.09)
@export var foliage_color: Color = Color(0.2, 0.5, 0.24)
@export var foliage_variation: Color = Color(0.14, 0.38, 0.18)
@export var seed_value: int = 2

func _ready() -> void:
	var trunk_material := TextureFactory.mottled_material(trunk_color, trunk_variation, 3.0, 0.95, 10, seed_value)
	var foliage_material := TextureFactory.mottled_material(foliage_color, foliage_variation, 2.0, 0.85, 5, seed_value + 1)

	($Trunk as MeshInstance3D).material_override = trunk_material
	($FoliageLow as MeshInstance3D).material_override = foliage_material
	($FoliageHigh as MeshInstance3D).material_override = foliage_material
