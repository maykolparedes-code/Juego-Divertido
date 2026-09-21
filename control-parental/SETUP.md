# Guía: instalar Android Studio y probar las apps

Esta guía asume que ya probaste el backend (ver `README.md`). Ahora vamos a
compilar y correr `mobile-child` y `mobile-parent` en tu computadora.

## 1. Instalar Android Studio

1. Descarga el instalador oficial desde
   [developer.android.com/studio](https://developer.android.com/studio)
   (gratis, de Google). Elige tu sistema operativo (Windows/Mac/Linux).
2. Instálalo con las opciones por defecto ("Standard install"). Esto
   incluye el SDK de Android, un emulador y las herramientas de línea de
   comandos que necesitas.
3. Al abrirlo por primera vez, el asistente descarga el SDK más reciente
   (puede tardar 10-20 minutos). Déjalo terminar.
4. Verifica que tengas **JDK 17** — Android Studio moderno lo trae
   incluido, no necesitas instalarlo aparte.

## 2. Abrir los proyectos

`mobile-child` y `mobile-parent` son dos proyectos de Android Studio
**independientes** (no un solo proyecto con dos módulos), para que puedas
tener ambos abiertos en dos ventanas al mismo tiempo — así se prueba de
verdad la interacción padre-hijo.

1. Abre Android Studio → **File → Open**.
2. Selecciona la carpeta `control-parental/mobile-child`.
3. Espera a que termine el "Gradle Sync" (barra de progreso abajo). La
   primera vez puede pedirte instalar el SDK de Android 34 o el
   "Android SDK Build-Tools" — acepta, Android Studio lo hace automático.
4. Repite lo mismo en una segunda ventana con `control-parental/mobile-parent`.

Si el sync falla la primera vez pidiendo una versión específica del SDK,
usa **Tools → SDK Manager** para instalar exactamente la versión que pide
(compileSdk 34).

## 3. Crear un emulador (o usar tu celular)

**Opción A — Emulador** (más simple para empezar):
1. **Tools → Device Manager → Create device**.
2. Elige cualquier teléfono (p. ej. Pixel 8) y una imagen de sistema
   Android 14 (API 34). Descárgala si te la pide.
3. Repite para tener **dos emuladores** corriendo a la vez (uno para
   "hijo", otro para "padre") — puedes clonar el mismo Device Manager dos
   veces con nombres distintos.

**Opción B — Tu celular real**: activa "Opciones de desarrollador" (Ajustes
→ Acerca del teléfono → toca 7 veces "Número de compilación") y luego
"Depuración USB". Conecta por cable; Android Studio lo detecta solo.

## 4. Levantar el backend (con base de datos real)

En tu computadora (no en este entorno de Claude):

```bash
cd control-parental/backend
npm install

# Necesitas PostgreSQL instalado y corriendo. Opción rápida con Docker:
docker run --name control-familiar-db -e POSTGRES_PASSWORD=dev -e POSTGRES_DB=control_familiar -p 5432:5432 -d postgres:16

cp .env.example .env
# Edita .env: DATABASE_URL="postgresql://postgres:dev@localhost:5432/control_familiar"
# y genera LOCATION_ENCRYPTION_KEY con:
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"

npx prisma db push       # crea las tablas
npx prisma db seed       # crea una familia/dispositivos de prueba — apunta los IDs que imprime
npm run start:dev
```

Déjalo corriendo. El emulador de Android ve tu backend local en
`http://10.0.2.2:3000/` (ya configurado en `BuildConfig.API_BASE_URL` de
ambas apps) — **no necesitas cambiar nada** si usas el emulador. Si usas un
celular real, tu backend debe ser accesible desde la red del celular
(mismo Wi-Fi que tu computadora, usando la IP local de tu máquina en vez
de `10.0.2.2`, editable en `build.gradle.kts` → `buildConfigField`).

## 5. Correr las apps

En cada ventana de Android Studio, botón ▶ (Run) con el emulador/celular
seleccionado arriba. La primera compilación tarda unos minutos.

**En la app del menor** (`mobile-child`):
1. Pega el `childDeviceId` y `familyId` que imprimió `prisma db seed` en
   los campos de la pantalla principal → **Vincular**.
2. Toca **Dar permiso de ubicación** y acepta.
3. Toca **Activar accesibilidad** → actívalo en la pantalla de Ajustes que
   se abre → vuelve atrás.
4. Toca **Activar acceso a uso de apps** → actívalo → vuelve atrás.
5. Toca **Iniciar monitoreo**: deberías ver la notificación persistente
   "Control Familiar · Ubicación activa" aparecer, no descartable.
6. Prueba el botón **SOS**.

**En la app del padre** (`mobile-parent`):
1. Pega el `familyId` (el mismo) → **Cargar familia**.
2. Deberías ver al hijo listado, con "Uso hoy" y si está dentro de una
   zona segura (se actualiza según los eventos de geocerca que dispare el
   dispositivo del menor).

Verifica en la terminal donde corre el backend (`npm run start:dev`) que
los `POST` van llegando — es la señal más rápida de que todo está
conectado.

## 6. Qué falta para un build de producción real

Esto es un scaffold funcional, no un producto terminado. Antes de instalar
esto en el teléfono de un hijo de verdad:

- [ ] **Pantalla de emparejamiento real**: hoy el "Device ID / Family ID"
      se pega a mano para poder probar. Construir el flujo de
      `AuthController.redeemInvite` con una pantalla de Compose que pida
      el código de invitación.
- [ ] **HTTPS**: cambiar `API_BASE_URL`/`WS_BASE_URL` a tu dominio propio
      con certificado válido, y quitar la excepción de `10.0.2.2` en
      `network_security_config.xml`.
- [ ] **Firebase Cloud Messaging** (alertas push en `mobile-parent`):
      crea un proyecto en [Firebase Console](https://console.firebase.google.com),
      descarga `google-services.json` a `mobile-parent/` (ya está en
      `.gitignore`, no lo subas), y agrega el plugin
      `com.google.gms:google-services` en `mobile-parent/build.gradle.kts`.
      Sin esto, la app compila pero las notificaciones push no llegan.
- [ ] **Cola de reintentos persistente**: `PendingLocationQueue` guarda en
      memoria; si la app se cierra se pierde. Reemplazar por Room +
      WorkManager (mencionado en el código).
- [ ] **Íconos y arte final**: los íconos actuales son vectores simples de
      relleno — usa el asistente "Image Asset" de Android Studio.
- [ ] **Persistencia real del backend en producción**: usa
      `prisma migrate deploy` (no `db push`, que es solo para desarrollo)
      contra tu base de datos administrada, con backups configurados.
- [ ] Ver también el checklist de seguridad en `ARCHITECTURE.md` (§8) y el
      recordatorio sobre documentar qué se recolecta, aunque sea de uso
      privado (`ARCHITECTURE.md` §9).
