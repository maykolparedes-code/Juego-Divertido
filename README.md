# Juego Divertido

Juego de tipo **endless runner** para Android hecho con Kotlin + Jetpack
Compose. El personaje avanza solo, tocas la pantalla para saltar y esquivar
obstáculos, y recoges monedas mientras la dificultad sube poco a poco.

Monetización integrada desde el día uno:

- **Anuncios (AdMob):** banner en menú/tienda, intersticial cada pocas
  partidas y anuncio recompensado opcional (revivir o duplicar monedas).
- **Compras dentro de la app (Google Play Billing):** quitar anuncios,
  paquetes de monedas y un pack de bienvenida.
- **Mejoras compradas con la moneda del juego** (se gana jugando, sin dinero
  real): doble salto, imán de monedas, vida extra, impulso inicial.

## Estructura del proyecto

```
gamecore/   Módulo Kotlin puro (sin Android) con el motor del juego:
            física, colisiones, dificultad, catálogo de mejoras y de
            productos de la tienda. Tiene tests unitarios (JUnit 5).
app/        App de Android: UI en Jetpack Compose, persistencia con
            DataStore, integración de AdMob y Play Billing.
```

Separar la lógica del juego (`gamecore`) de la app de Android permite
probarla con JUnit puro, sin depender del SDK de Android ni de un
emulador.

## Requisitos para compilar

- **Android Studio** (Koala o más reciente) con el SDK de Android
  (`compileSdk 34`, `minSdk 24`).
- JDK 17.

Este proyecto se generó en un entorno sin el SDK de Android instalado, así
que **no se pudo compilar ni ejecutar la app completa aquí**. Sí se
verificó con tests unitarios reales (`./gradlew :gamecore:test`, 18 tests,
todos en verde) toda la lógica del juego: saltos, colisiones, puntaje,
mejoras y catálogo de la tienda. Antes de publicar, ábrelo en Android
Studio y compila/ejecuta la app completa al menos una vez para detectar
cualquier detalle específico de la versión de SDK que tengas instalada.

```bash
# Lógica del juego (no requiere SDK de Android)
./gradlew :gamecore:test

# App completa (requiere Android Studio / SDK de Android)
./gradlew :app:assembleDebug
```

## Cómo jugar

Toca la pantalla para saltar. Esquiva los obstáculos rojos y recoge las
monedas amarillas. La velocidad aumenta con el tiempo. Al perder, puedes
reintentar, ver un anuncio para continuar la partida, o ver otro anuncio
para duplicar las monedas ganadas en esa partida.

## Configurar AdMob (antes de publicar)

La app usa por defecto los **IDs de anuncio de PRUEBA públicos de Google**
(ver `app/src/main/java/com/maykol/juegodivertido/AppConstants.kt` y el
`APPLICATION_ID` en `AndroidManifest.xml`). Son seguros para desarrollar y
probar: siempre muestran anuncios de ejemplo y nunca arriesgan tu cuenta.

Antes de publicar:

1. Crea una cuenta en [AdMob](https://apps.admob.com) y registra la app.
2. Crea 3 unidades de anuncio: banner, intersticial y recompensado.
3. Reemplaza:
   - El `APPLICATION_ID` en `app/src/main/AndroidManifest.xml`.
   - Las 4 constantes en `AppConstants.kt` (`AdUnitIds.BANNER`,
     `AdUnitIds.INTERSTITIAL`, `AdUnitIds.REWARDED`).
4. Prueba con [dispositivos de prueba de AdMob](https://developers.google.com/admob/android/test-ads#enable_test_devices)
   antes de lanzar, para no infringir las políticas de Google haciendo clic
   en tus propios anuncios reales.

## Configurar las compras dentro de la app (Google Play Billing)

Los IDs de producto que la app espera están en `gamecore` (
`com.maykol.juegodivertido.core.StoreCatalog`):

| ID de producto      | Tipo            | Qué otorga                              |
|----------------------|-----------------|-------------------------------------------|
| `remove_ads`         | No consumible   | Quita banners e intersticiales            |
| `coins_small_500`    | Consumible      | 500 monedas                               |
| `coins_medium_1500`  | Consumible      | 1500 monedas                              |
| `coins_large_5000`   | Consumible      | 5000 monedas                              |
| `starter_pack`       | No consumible   | Quita anuncios + 1000 monedas + skin      |

Pasos:

1. Sube al menos un APK/AAB firmado a un track interno de Play Console
   (las compras no funcionan hasta que la app existe ahí).
2. En **Monetización > Productos dentro de la app**, crea cada producto
   con exactamente esos IDs.
3. Añade cuentas de prueba en **Configuración > Pruebas de licencia** para
   poder comprar sin que se te cobre de verdad mientras pruebas.

### Verificación de compras (seguridad)

La app reconoce/consume las compras localmente con Play Billing Library,
que ya valida contra los servidores de Google. Para una tienda con
productos de mayor valor, se recomienda además:

- Verificar el recibo de cada compra en tu propio backend usando la
  [Google Play Developer API](https://developer.android.com/google/play/billing/security#verify)
  antes de otorgar la recompensa, en vez de confiar solo en el cliente.
- Suscribirte a las [Real-time Developer Notifications](https://developer.android.com/google/play/billing/rtdn-reference)
  para detectar reembolsos y revocar lo comprado.

Esto no está implementado en este proyecto (requeriría un backend propio),
pero es el siguiente paso recomendado antes de escalar los ingresos.

## Seguridad y buenas prácticas ya aplicadas

- Sin tráfico sin cifrar (`usesCleartextTraffic="false"`).
- Permisos mínimos: solo `INTERNET` y `ACCESS_NETWORK_STATE` (Billing
  añade `com.android.vending.BILLING` automáticamente).
- ProGuard/R8 activado en release (`isMinifyEnabled`, `isShrinkResources`)
  con reglas en `app/proguard-rules.pro` para no romper AdMob/Billing y
  para eliminar logs de depuración del build final.
- `.gitignore` excluye `local.properties`, keystores (`*.jks`,
  `*.keystore`) y `key.properties`: **nunca subas tu clave de firma ni tus
  credenciales al repositorio**.
- El progreso del jugador se persiste localmente con DataStore; no se
  envía a ningún servidor propio (este proyecto no tiene backend).

## Antes de publicar en Google Play

- [ ] Reemplazar los IDs de anuncio de prueba por los reales (ver arriba).
- [ ] Crear los productos dentro de la app en Play Console con los IDs
      exactos de `StoreCatalog`.
- [ ] Generar tu propio keystore de firma (`keytool -genkeypair …`) y
      configurar `signingConfigs` en `app/build.gradle.kts` — nunca
      compartas ni subas ese archivo.
- [ ] Reemplazar el icono de lanzador (`app/src/main/res/mipmap-anydpi-v26`
      y `drawable/ic_launcher_*`) por arte final, usando el asistente
      "Image Asset" de Android Studio.
- [ ] Publicar una **política de privacidad** (obligatoria por usar AdMob y
      Billing) y enlazarla desde la ficha de la app en Play Console.
- [ ] Completar el formulario de **Target audience & content** y el
      **Data safety** de Play Console (la app recoge datos de publicidad a
      través del SDK de AdMob).
- [ ] Probar un build de release real (`./gradlew :app:bundleRelease`) en
      un dispositivo físico antes de subirlo.

## Ideas para seguir mejorando el juego

- Arte y animaciones propias en vez de las formas geométricas actuales.
- Efectos de sonido y música (con opción de silenciar).
- Más skins de personaje desbloqueables.
- Tabla de puntajes online (requeriría backend, p. ej. Firebase).
- A/B testing de la frecuencia de intersticiales para maximizar ingresos
  sin dañar la retención.
