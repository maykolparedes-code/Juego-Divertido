# Aventura 3D — El Príncipe

Juego de aventura en 3D para Android hecho con **Godot 4.3** (motor
gratuito y de código abierto). Es un proyecto nuevo y separado del runner
2D (`app/` + `gamecore/` en la raíz del repo); no lo toca ni depende de él.

## Qué incluye ahora mismo

**Dos niveles** jugables de principio a fin, con el mismo bucle de misión:

1. **Nivel 1 — La Isla:** hablas con el aldeano → te pide 3 cristales →
   los recolectas explorando la isla → vuelves → se abre la puerta del
   templo → un pasaje secreto te lleva al Nivel 2.
2. **Nivel 2 — El Bosque Encantado:** un Sabio te pide 3 gemas mágicas
   escondidas entre árboles más densos → las recolectas → vuelves → se
   abre la puerta final → recompensa y cierre de esta demo.

Con guardado de progreso local (persiste entre sesiones, incluyendo hasta
qué nivel llegaste), cámara en tercera persona controlable, y controles
táctiles pensados para Android (joystick virtual + arrastre para la
cámara + botones de salto/ataque/interactuar).

## El personaje: el Príncipe

Corona, capa, túnica con textura, y arma de verdad: una espada envainada
al costado (se blande con el botón "Atacar" — por ahora es una animación,
todavía no hay combate/enemigos) y un escudo al otro brazo. El aldeano y
el Sabio usan la misma base de personaje sin el equipo del príncipe.

## Sobre los gráficos: por qué son low-poly "de cápsulas" y no realistas

No puedo generar modelos 3D con texturas fotorrealistas ni descargar
bancos de assets (probé conectarme a Kenney.nl, OpenGameArt, itch.io y
Quaternius desde este entorno de desarrollo — los cuatro están bloqueados
por la política de red de aquí, igual que pasó antes con el SDK de
Android). Así que el personaje, el Sabio/aldeano, los árboles, rocas y el
suelo se construyen **por código**:

- **Formas:** cápsulas y esferas (`scripts/character_builder.gd`) en vez
  de simples cajas, con ojos y equipo (corona, capa, espada, escudo)
  también generados por código.
- **Texturas:** no son de un archivo de imagen — se generan en tiempo de
  carga con un algoritmo de "manchas suaves" (`scripts/texture_factory.gd`,
  una variante simple de ruido tipo Perlin) para que el pasto, la piedra,
  la corteza y la túnica no se vean de color plano.
- **Animación:** ciclo de caminar/idle y el golpe de espada, todo por
  código rotando los "huesos" (pivotes) de brazos y piernas con funciones
  seno, sin necesitar un `AnimationPlayer` con keyframes.
- **Ambiente:** cielo con degradado cálido, niebla sutil, brillo/bloom en
  los cristales y el tesoro, luz solar cálida — todo configuración de
  `Environment`, no assets.

Es el techo real de "gráficos con textura y diseño" alcanzable sin poder
bajar arte de internet ni generar modelos 3D. Para dar el salto a un
personaje con textura pintada a mano y animaciones de verdad (caminar,
correr, atacar con keyframes reales), el camino es importar un modelo
`.glb`/`.gltf` — ver la sección siguiente.

**Para mejorar el arte más adelante:** consigue un personaje low-poly
rigueado y animado (por ejemplo, exportado desde
[Mixamo](https://www.mixamo.com) en formato `.glb`, o un pack de una
tienda de assets), impórtalo a `res://assets/`, y reemplaza la llamada a
`CharacterBuilder.build_prince()` en `player.gd` (o `CharacterBuilder.build()`
en `npc.gd`) por una instancia de ese modelo con su propio
`AnimationPlayer`/`AnimationTree`. El resto del juego (cámara, controles,
misión, diálogo, niveles, guardado) no necesita cambiar — están escritos
contra los nombres de nodos (`LeftArmPivot`, etc.), así que si tu modelo
importado expone un esqueleto real, solo tendrías que adaptar
`_animate()` para usar sus animaciones en vez de los pivotes.

## Estructura del proyecto

```
aventura-3d/
  project.godot              Configuración del proyecto, input map, autoloads
  scenes/
    Level1.tscn, Level2.tscn   Los dos niveles jugables
    Player.tscn, NPC.tscn, Crystal.tscn, Gate.tscn, VictoryZone.tscn
    Tree.tscn, Rock.tscn        Props reutilizables entre niveles
    UI/                         HUD, cuadro de diálogo, controles táctiles
  scripts/                    Toda la lógica de gameplay
    character_builder.gd        Construye personajes por código
    texture_factory.gd          Genera texturas por código
    textured_ground.gd/_tree.gd/_rock.gd   Aplican esas texturas
  scripts/autoload/
    game_state.gd                Misión, cristales, guardado
    dialogue_service.gd          Mediador para mostrar diálogos
    level_manager.gd             Sabe qué nivel sigue y cambia de escena
  tests/                      Prueba de humo automatizada (ver abajo)
  export_presets.cfg          Preset de exportación a Android (debug)
```

## Controles

- **Escritorio (para probar en el editor de Godot):** WASD para moverte,
  Espacio para saltar, F para atacar, E para interactuar, botón derecho
  del mouse + arrastrar para mover la cámara.
- **Android:** joystick virtual abajo a la izquierda, arrastra con el dedo
  en el lado derecho de la pantalla para la cámara, botones "Saltar" y
  "Atacar", y botón "Hablar" que aparece solo cuando estás cerca de
  alguien con quien interactuar.

## Cómo abrirlo y jugarlo

1. Instala [Godot 4.3](https://godotengine.org/download) (la versión
   estándar, no la de .NET).
2. Abre Godot → "Importar" → selecciona `aventura-3d/project.godot`.
3. Pulsa ▶ (F5) para jugar dentro del editor.

## Prueba automatizada

`tests/SmokeTest.tscn` + `tests/smoke_test.gd` simulan una partida
completa de **ambos niveles** (hablar con el NPC, recolectar los 3
objetos, volver, comprobar que la puerta se abre) sin necesitar que una
persona juegue manualmente. Se ejecuta así:

```bash
godot --headless res://tests/SmokeTest.tscn
```

Termina con código de salida `0` si todo pasa, o `1` si algo falla (y
detalla qué). Este proyecto se validó así antes de entregarse en cada
versión: no es solo "no se cae al abrir", sino que el flujo completo de
la misión, en los dos niveles, se comprobó de verdad — incluyendo un test
de regresión específico para el bug de controles táctiles que se reportó
jugando en un dispositivo real.

## Exportar un APK (Android)

Ya existe un preset de depuración (`export_presets.cfg`, apuntando a un
keystore de debug). Necesitas Android SDK configurado en Godot (Editor →
Editor Settings → Export → Android → Android SDK Path) y las plantillas
de exportación de Android instaladas (Godot te las ofrece descargar la
primera vez que exportas). Luego: Proyecto → Exportar → Android → Exportar
proyecto.

Este repositorio también incluye un workflow de GitHub Actions
(`.github/workflows/build-godot-apk.yml`) que compila un APK de depuración
automáticamente en cada push a esta rama y lo publica como GitHub Release,
para no depender de tener Godot instalado localmente.

## Exportar para iPhone (iOS) — lo que hace falta de tu lado

**No puedo generarte un .ipa instalable desde este entorno de desarrollo**,
y no es un límite de esfuerzo: es cómo funciona la firma de apps de Apple.
A diferencia de Android (donde cualquiera puede compilar un APK e
instalarlo activando "orígenes desconocidos"), iOS exige que **todo** el
que compila para un iPhone real tenga una identidad de Apple:

1. **Necesitas una Mac con Xcode instalado.** Godot puede *generar* el
   proyecto de Xcode desde cualquier sistema, pero **compilarlo y
   firmarlo requiere Xcode**, que solo corre en macOS. No hay manera de
   evitarlo, ni yo ni ningún otro servicio en la nube sin macOS puede
   saltarse este paso (los runners de GitHub Actions sí ofrecen macOS,
   pero igual necesitarían tus credenciales de Apple para firmar).
2. **Necesitas una cuenta de Apple** ligada a esa Mac/Xcode. Dos niveles:
   - **Gratis (Apple ID normal):** puedes compilar e instalar en tu
     propio iPhone conectándolo por cable a la Mac, pero la app deja de
     abrir a los **7 días** y hay que reinstalarla así cada vez.
   - **Apple Developer Program (99 USD/año):** firma que dura, permite
     TestFlight (compartir la app con otras personas sin cable) y es
     obligatorio si algún día quieres publicarla en la App Store.

**Cuando tengas la Mac + cuenta lista**, el proceso es:

1. Abre este proyecto en Godot en esa Mac.
2. Proyecto → Exportar → Añadir... → iOS (la primera vez, Godot te pide
   las plantillas de exportación de iOS, que descarga solo).
3. En las opciones del preset, indica tu **Team ID** de Apple (Editor →
   Editor Settings → Export → iOS, o directamente en el preset).
4. Exportar genera un proyecto de Xcode (`.xcodeproj`); ábrelo en Xcode,
   selecciona tu equipo de firma, conecta el iPhone y pulsa ▶ para
   instalar, o Archive → Distribute App para TestFlight/App Store.

Si en algún momento consigues la cuenta y quieres que te ayude con la
parte de código (no con la firma, que es tuya por diseño de Apple), puedo:
automatizar el export en un runner de GitHub Actions con macOS usando tus
certificados como *secrets* del repositorio, o revisar/ajustar el preset
de iOS contigo mientras lo pruebas en tu Mac.

## Qué falta para monetizar (siguiente paso)

Este juego todavía no tiene anuncios ni compras dentro de la app. Para
generar ingresos como en el runner 2D, el camino recomendado en Godot es:

- **Anuncios:** un plugin de Android para Godot como
  [Godot Android AdMob Plugin](https://github.com/Shin-NiL/Godot-Android-Admob-Plugin)
  (comunidad, no oficial de Google) — requiere activar "Custom Build" /
  Gradle en el export de Android.
- **Compras:** un plugin equivalente para Google Play Billing.

Ambos requieren "Custom Build" (Gradle + Android SDK completo), a
diferencia del export por defecto usado aquí. Se puede añadir cuando
quieras avanzar con la monetización de este juego — dilo y lo armamos
igual que hicimos con el runner 2D.

## Ideas para seguir mejorando (no incluidas todavía)

- Combate real: que el golpe de espada dañe enemigos, con vida/derrota.
- Enemigos y obstáculos que se muevan.
- Sonido y música (no se agregó audio en esta versión).
- Más niveles reutilizando el mismo LevelManager (solo hay que crear
  `Level3.tscn` siguiendo el patrón de los dos existentes y agregarlo a
  la lista `LEVELS` en `scripts/autoload/level_manager.gd`).
- Selector de nivel / menú principal (hoy el juego arranca directo en el
  Nivel 1).

## Notas de seguridad

- El guardado (`user://savegame.json`) es local al dispositivo; no se
  envía a ningún servidor (este proyecto no tiene backend).
- El keystore de depuración (`~/.android/debug.keystore` o el que use tu
  máquina/CI) **nunca debe usarse para publicar en Google Play**: antes de
  publicar, genera tu propio keystore de release y no lo subas al
  repositorio (ver `.gitignore`, que ya excluye `*.keystore`/`*.jks`).
- Lo mismo aplica a certificados/perfiles de iOS si en el futuro se
  automatiza ese export: nunca se deben commitear al repositorio, solo
  guardarse como *secrets* de CI.
