class_name TextureFactory
extends RefCounted

## Genera texturas por código (manchas suaves tipo "value noise" y
## degradados) para que los materiales no se vean de color plano. No hay
## forma de descargar bancos de texturas/modelos en este entorno (la red
## está restringida a unos pocos dominios de desarrollo), así que esto es
## el camino disponible para un look "con textura" sin archivos de imagen
## externos.

## Textura con manchas suaves entre dos colores (útil para pasto, piedra,
## corteza de árbol, tela...). [cells] controla el tamaño de las manchas:
## números bajos = manchas grandes, números altos = detalle más fino.
static func mottled_texture(
	base_color: Color,
	variation_color: Color,
	size: int = 64,
	cells: int = 6,
	seed_value: int = 0
) -> ImageTexture:
	var rng := RandomNumberGenerator.new()
	rng.seed = seed_value

	# Una grilla de valores aleatorios más pequeña que la imagen final; se
	# interpola (bilinear) al pintar cada píxel para lograr manchas suaves
	# en vez de "estática" píxel a píxel.
	var grid_size := cells + 1
	var grid := PackedFloat32Array()
	grid.resize(grid_size * grid_size)
	for i in grid.size():
		grid[i] = rng.randf()

	var image := Image.create_empty(size, size, false, Image.FORMAT_RGB8)
	for y in size:
		for x in size:
			var u: float = float(x) / float(size) * cells
			var v: float = float(y) / float(size) * cells
			var x0 := int(floor(u))
			var y0 := int(floor(v))
			var x1: int = mini(x0 + 1, cells)
			var y1: int = mini(y0 + 1, cells)
			var fx: float = u - x0
			var fy: float = v - y0
			var top: float = lerp(grid[y0 * grid_size + x0], grid[y0 * grid_size + x1], fx)
			var bottom: float = lerp(grid[y1 * grid_size + x0], grid[y1 * grid_size + x1], fx)
			var value: float = lerp(top, bottom, fy)
			image.set_pixel(x, y, base_color.lerp(variation_color, value))

	var texture := ImageTexture.create_from_image(image)
	return texture

## Degradado vertical simple (útil para capas, cielo dentro de una gema, etc.).
static func vertical_gradient(top_color: Color, bottom_color: Color, size: int = 32) -> ImageTexture:
	var image := Image.create_empty(1, size, false, Image.FORMAT_RGB8)
	for y in size:
		var t := float(y) / float(size - 1)
		image.set_pixel(0, y, top_color.lerp(bottom_color, t))
	return ImageTexture.create_from_image(image)

## Atajo: aplica una textura moteada a un material nuevo, con el repetido
## (tiling) ya configurado, listo para usar como material_override.
static func mottled_material(
	base_color: Color,
	variation_color: Color,
	uv_scale: float = 4.0,
	roughness: float = 0.85,
	cells: int = 6,
	seed_value: int = 0
) -> StandardMaterial3D:
	var material := StandardMaterial3D.new()
	material.albedo_texture = mottled_texture(base_color, variation_color, 64, cells, seed_value)
	material.uv1_scale = Vector3(uv_scale, uv_scale, 1.0)
	material.roughness = roughness
	return material
