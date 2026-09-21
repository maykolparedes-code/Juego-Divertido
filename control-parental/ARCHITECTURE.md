# Arquitectura técnica — Control Familiar

## 1. Visión general

Dos apps Android nativas (Kotlin + Jetpack Compose) que comparten un
backend central:

```
┌─────────────────┐        HTTPS/WSS (TLS 1.3)        ┌──────────────────┐
│  App del Padre   │ ───────────────────────────────► │                  │
│  (mobile-parent)  │ ◄─────────────────────────────── │     Backend      │
└─────────────────┘        push (FCM)                 │  NestJS + REST   │
                                                        │  + WebSocket     │
┌─────────────────┐        HTTPS/WSS (TLS 1.3)        │  + PostgreSQL    │
│  App del Menor   │ ───────────────────────────────► │                  │
│  (mobile-child)   │ ◄─────────────────────────────── │                  │
└─────────────────┘        push (FCM)                 └──────────────────┘
```

- **Frontend**: Android nativo, Kotlin + Jetpack Compose, arquitectura
  MVVM, `StateFlow` para estado reactivo, WorkManager para tareas en
  segundo plano, DataStore para preferencias locales.
- **Backend**: NestJS (Node.js + TypeScript), API REST para operaciones
  CRUD y un gateway WebSocket para ubicación en tiempo real y alertas.
- **Base de datos**: PostgreSQL con PostGIS (índices geoespaciales para
  ubicación y geocercas) + Redis para sesiones WebSocket activas y colas.
- **Notificaciones push**: Firebase Cloud Messaging, para avisar al padre
  de alertas SOS/geocerca sin mantener la app abierta, y para despertar el
  servicio de ubicación del menor si el sistema lo mató.

## 2. Roles

| Rol | App | Puede |
|---|---|---|
| Padre/Tutor | `mobile-parent` | Ver ubicación y mapa de geocercas, configurar límites de tiempo, ver reportes de uso, recibir alertas SOS y de geocerca, administrar dispositivos vinculados a la familia |
| Menor | `mobile-child` | Ver su propio estado (igual que Family Link/Screen Time: qué se comparte y por qué), usar el botón SOS, solicitar tiempo extra a un padre |

La app del menor **nunca oculta su ícono ni finge estar desinstalada**.
Tiene una pantalla de "Qué comparte esta app" siempre accesible, en
lenguaje adecuado a la edad.

## 3. Modelo de datos (PostgreSQL vía Prisma)

Ver [`backend/prisma/schema.prisma`](./backend/prisma/schema.prisma) para
el esquema completo. Entidades principales:

- `Family` — agrupa padres e hijos, tiene un código de invitación para
  vincular dispositivos.
- `User` — rol `PARENT` o `CHILD`.
- `Device` — dispositivo físico vinculado, con `pushToken` para FCM.
- `LocationPing` — puntos de ubicación, **cifrados en reposo** por familia.
- `Geofence` / `GeofenceEvent` — zonas seguras y eventos de entrada/salida.
- `ScreenTimeRule` — límites diarios por app o categoría, por horario.
- `AppUsageSession` — sesiones de uso reales medidas en el dispositivo.
- `Alert` — SOS, entrada/salida de geocerca, límite alcanzado.

## 4. Ubicación en tiempo real

**Decisión de diseño clave**: la evaluación de geocercas ocurre en el
**dispositivo del menor** (Android Geofencing API, sobre Google Play
Services), no en el servidor. Esto:

- Reduce el consumo de batería y datos (el sistema operativo despierta la
  app solo en transiciones de entrada/salida, no por cada punto GPS).
- Minimiza qué ubicación cruda sale del dispositivo: solo se envían
  eventos de geocerca y pings periódicos de baja frecuencia para el mapa
  en vivo, no un stream continuo de coordenadas exactas.
- Es el patrón recomendado por Google para este caso de uso.

El servidor solo recibe:
1. Pings de ubicación de baja frecuencia (configurable, p. ej. cada 2–5
   min, o bajo demanda cuando el padre abre el mapa en vivo) para pintar
   el mapa.
2. Eventos de geocerca (`ENTER`/`EXIT`) ya calculados en el dispositivo.
3. Alertas SOS con la ubicación exacta en el momento del evento.

Ver código de ejemplo en
[`mobile-child/.../location/LocationForegroundService.kt`](./mobile-child/src/main/java/com/maykol/controlfamiliar/child/location/LocationForegroundService.kt).

### Notificación persistente (obligatoria por el SO)

Desde Android 8 (API 26), cualquier servicio que acceda a ubicación en
segundo plano debe ejecutarse como **foreground service** con una
notificación persistente, no descartable por el usuario mientras el
servicio está activo. No es opcional ni se puede suprimir: es la garantía
del sistema operativo de que nadie es rastreado sin saberlo. La
implementación de ejemplo usa un canal de notificación claro
("Control Familiar · Ubicación activa") con acción rápida para abrir la
app y ver qué se comparte.

## 5. Gestión de tiempo de pantalla y bloqueo de apps

Dos permisos de Android, ambos con pantalla de consentimiento explícita
mostrada al menor durante el onboarding:

- **`UsageStatsManager`** (permiso `PACKAGE_USAGE_STATS`, se concede desde
  Ajustes → Acceso especial): para medir tiempo real de uso por app y
  generar los reportes por categoría.
- **`AccessibilityService`**: para detectar qué app está en primer plano
  en tiempo real y mostrar una pantalla de bloqueo (`BlockingOverlayActivity`)
  cuando se supera el límite configurado. Al activarlo, Android muestra un
  aviso del sistema explicando qué puede hacer el servicio — no se puede
  ni se debe evitar.

Ver
[`mobile-child/.../screentime/AppUsageAccessibilityService.kt`](./mobile-child/src/main/java/com/maykol/controlfamiliar/child/screentime/AppUsageAccessibilityService.kt).

## 6. Botón de pánico / SOS

Botón siempre visible en la pantalla principal de la app del menor.
Al pulsarlo:
1. Captura ubicación de alta precisión inmediata (`Priority.HIGH_ACCURACY`).
2. Envía `POST /alerts` al backend con tipo `SOS`.
3. El backend publica el evento por WebSocket a los dispositivos del/los
   padre(s) conectados y dispara un push FCM de alta prioridad
   (`priority: high`, canal de alertas con sonido) para que llegue incluso
   con la app cerrada.

## 7. Reportes de uso por categoría

`AppUsageSession` se agrega diariamente por categoría (`EDUCATION`,
`GAMES`, `SOCIAL`, `ENTERTAINMENT`, `OTHER` — catálogo mapeado desde el
`packageName` vía la API de Play Store o una tabla propia editable por el
padre). El endpoint `GET /usage-reports/:deviceId?range=week` devuelve
series agregadas para las gráficas del dashboard.

## 8. Seguridad y cifrado

- **En tránsito**: TLS 1.3 obligatorio (HSTS, `usesCleartextTraffic=false`
  en ambas apps Android), certificado pinning opcional en las apps móviles
  contra el dominio propio del backend.
- **En reposo**: las columnas sensibles (`LocationPing.lat/lng`,
  `Alert.lat/lng`) se cifran con AES-256-GCM a nivel de aplicación antes
  de insertarse, con una clave por familia derivada de un KMS (o, en
  desarrollo, de una variable de entorno). Ver
  [`backend/src/common/encryption/encryption.service.ts`](./backend/src/common/encryption/encryption.service.ts).
- **Nota honesta sobre "E2EE" real**: un cifrado extremo-a-extremo puro
  (donde ni el propio backend puede leer los datos) es incompatible con
  que el servidor pinte el mapa en vivo o calcule reportes agregados en el
  servidor. Por eso el cálculo de geocercas se hace en el dispositivo (ver
  §4) y el servidor solo actúa como almacén cifrado en reposo + relé en
  tránsito cifrado — el mejor equilibrio real entre privacidad y
  funcionalidad, y el mismo que usan Family Link/Life360.
- **Autenticación**: JWT de corta duración + refresh token, vínculo
  dispositivo-familia mediante código de invitación de un solo uso
  generado por el padre.
- **Permisos**: cada permiso sensible (ubicación en segundo plano,
  accesibilidad, uso de apps, notificaciones) se solicita con una pantalla
  previa propia que explica, en lenguaje simple, para qué se usa —
  adicional a los diálogos del sistema operativo, nunca en su lugar.

## 9. Compartir pantalla — bajo pedido, nunca automático

Igual que la ubicación (§4), esta función se diseñó para que sea
**imposible** activarla sin que el menor lo sepa y decida — no por buena
voluntad del código, sino porque Android mismo lo exige en cada paso:

1. El padre pide ver la pantalla desde `mobile-parent`
   (`ScreenShareViewerScreen.kt`). El backend (`screenshare.gateway.ts`)
   solo reenvía ese pedido por WebSocket a la familia — nunca autoriza
   nada por su cuenta, es un simple relé entre dos consentimientos.
2. El menor ve un diálogo propio de la app
   (`MainActivity.kt` en `mobile-child`) explicando qué está pidiendo su
   familia, con botones **Aceptar** / **Rechazar**. Si rechaza, ahí
   termina — el backend nunca ve un solo cuadro.
3. Si acepta, Android exige un **segundo consentimiento que esta app no
   puede saltarse ni pre-aprobar**: el diálogo de sistema de
   `MediaProjectionManager` ("¿Empezar a grabar o transmitir?"). Sin ese
   "Sí" del propio sistema operativo, no se puede crear ni un solo
   `VirtualDisplay`.
4. Mientras dura, `ScreenShareForegroundService` corre como foreground
   service tipo `mediaProjection`, con la notificación persistente
   obligatoria de Android ("Compartiendo pantalla en vivo") y un botón
   **Detener** tanto ahí como en la app — el menor corta la sesión cuando
   quiera, no solo al principio.

**Limitación honesta de este scaffold**: los "cuadros" que viajan por
`screen-share:frame` son capturas JPEG periódicas (≈1 por segundo), no un
video continuo de baja latencia — suficiente para supervisión puntual
("¿qué está viendo ahora mismo?"), no para ver algo que se mueve rápido
con fluidez. Implementarlo así fue una decisión deliberada: no requiere
infraestructura adicional (señalización WebRTC, servidores STUN/TURN,
la librería nativa de WebRTC en Android) y es auditable con una lectura
simple del código. Si en el futuro se necesita video real, la ruta
recomendada es migrar `screen-share:frame` a una sesión WebRTC completa
manteniendo exactamente el mismo flujo de doble consentimiento descrito
arriba — eso no cambia.

## 10. Por qué no hay política de privacidad "porque es de uso particular"

Aun sin publicarse en una tienda de apps, si vas a operar esto con datos
reales de una persona (aunque sea tu hijo/a) recomendamos igualmente
documentar en un archivo interno (`PRIVACY.md`, no público) qué se
recolecta, por cuánto tiempo se guarda y quién tiene acceso — te protege
a ti legalmente y le da al menor, según su edad, algo concreto que le
puedas mostrar cuando pregunte "¿qué ven exactamente de mí?". No se
incluye aquí porque depende de tu país y la edad de cada hijo/a.
