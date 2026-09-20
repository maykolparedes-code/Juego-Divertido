# Aventura 3D

Primer "vertical slice" de un juego de aventura en 3D para Android, hecho
con **Godot 4.3** (motor gratuito y de código abierto). Es un proyecto
nuevo y separado del runner 2D (`app/` + `gamecore/` en la raíz del repo);
no lo toca ni depende de él.

## Qué incluye ahora mismo

Un bucle de misión completo, jugable de principio a fin:

1. Apareces en una pequeña isla explorable en tercera persona.
2. Hablas con el aldeano (NPC) → te pide 3 cristales.
3. Exploras la isla y los recolectas.
4. Vuelves con el aldeano → se abre la puerta del templo.
5. Entras al templo y encuentras el tesoro final.

Con guardado de progreso local (persiste entre sesiones), cámara en
tercera persona controlable, y controles táctiles pensados para Android
(joystick virtual + arrastre para la cámara + botones de salto/interactuar).

## Por qué el personaje se ve "de cajas" (low-poly)

No puedo generar modelos 3D con texturas ni animaciones realistas, así que
el personaje y el aldeano se construyen **por código**, combinando cajas de
colores (`scripts/character_builder.gd`), con un ciclo de caminar/idle
animado también por código (rotando los "brazos" y "piernas" con
funciones seno, ver `_animate()` en `scripts/player.gd`). Es 100%
funcional y sin dependencias externas, pero es arte de marcador de
posición.

**Para mejorar el arte más adelante:** puedes descargar un personaje
low-poly rigueado y animado (por ejemplo, un pack gratuito de
[Kenney.nl](https://kenney.nl) o un modelo animado con
[Mixamo](https://www.mixamo.com), exportado en formato `.glb`/`.gltf`),
importarlo a `res://assets/`, y reemplazar la llamada a
`CharacterBuilder.build(...)` en `player.gd`/`npc.gd` por una instancia de
ese modelo con su propio `AnimationPlayer`/`AnimationTree`. El resto del
juego (cámara, controles, misión, diálogo, guardado) no necesita cambiar.

## Estructura del proyecto

```
aventura-3d/
  project.godot            Configuración del proyecto, input map, autoloads
  scenes/                  Escenas: Player, NPC, Crystal, Gate, World...
  scenes/UI/                HUD, cuadro de diálogo, controles táctiles
  scripts/                  Toda la lógica de gameplay
  scripts/autoload/          GameState (misión/guardado) y DialogueService
  tests/                    Prueba de humo automatizada (ver abajo)
  export_presets.cfg        Preset de exportación a Android (debug)
```

## Controles

- **Escritorio (para probar en el editor de Godot):** WASD para moverte,
  Espacio para saltar, E para interactuar, botón derecho del mouse +
  arrastrar para mover la cámara.
- **Android:** joystick virtual abajo a la izquierda, arrastra con el dedo
  en el lado derecho de la pantalla para la cámara, botón "Saltar", y
  botón "Hablar" que aparece solo cuando estás cerca de alguien con quien
  interactuar.

## Cómo abrirlo y jugarlo

1. Instala [Godot 4.3](https://godotengine.org/download) (la versión
   estándar, no la de .NET).
2. Abre Godot → "Importar" → selecciona `aventura-3d/project.godot`.
3. Pulsa ▶ (F5) para jugar dentro del editor.

## Prueba automatizada

`tests/SmokeTest.tscn` + `tests/smoke_test.gd` simulan una partida
completa (hablar con el NPC, recolectar los 3 cristales, volver, comprobar
que la puerta se abre) sin necesitar que una persona juegue manualmente.
Se ejecuta así:

```bash
godot --headless res://tests/SmokeTest.tscn
```

Termina con código de salida `0` si todo pasa, o `1` si algo falla (y
detalla qué). Este proyecto se validó así antes de entregarse: no es
solo "no se cae al abrir", sino que el flujo completo de la misión se
comprobó de verdad.

## Exportar un APK

Ya existe un preset de depuración (`export_presets.cfg`, apuntando a un
keystore de debug). Necesitas Android SDK configurado en Godot (Editor →
Editor Settings → Export → Android → Android SDK Path) y las plantillas
de exportación de Android instaladas (Godot te las ofrece descargar la
primera vez que exportas). Luego: Proyecto → Exportar → Android → Exportar
proyecto.

Este repositorio también incluye un workflow de GitHub Actions
(`.github/workflows/build-godot-apk.yml`) que compila un APK de depuración
automáticamente en cada push a esta rama y lo deja como artefacto
descargable, para no depender de tener Godot instalado localmente.

## Qué falta para monetizar (siguiente paso)

Este slice es solo la jugabilidad principal. Para generar ingresos con
anuncios (AdMob) y compras dentro de la app, como en el runner 2D, el
camino recomendado en Godot es:

- **Anuncios:** un plugin de Android para Godot como
  [Godot Android AdMob Plugin](https://github.com/Shin-NiL/Godot-Android-Admob-Plugin)
  (comunidad, no oficial de Google) — requiere activar "Custom Build" /
  Gradle en el export de Android.
- **Compras:** un plugin equivalente para Google Play Billing.

Ambos requieren "Custom Build" (Gradle + Android SDK completo), a
diferencia del export por defecto usado aquí. Se puede añadir cuando
quieras avanzar con la monetización de este juego — dilo y lo armamos
igual que hicimos con el runner 2D.

## Notas de seguridad

- El guardado (`user://savegame.json`) es local al dispositivo; no se
  envía a ningún servidor (este proyecto no tiene backend).
- El keystore de depuración (`~/.android/debug.keystore` o el que use tu
  máquina/CI) **nunca debe usarse para publicar en Google Play**: antes de
  publicar, genera tu propio keystore de release y no lo subas al
  repositorio (ver `.gitignore`, que ya excluye `*.keystore`/`*.jks`).
