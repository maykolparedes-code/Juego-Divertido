# Control Familiar — Control Parental y Bienestar Digital

Aplicación de control parental **transparente**: el menor siempre sabe que
está instalada (ícono propio, nombre claro) y, mientras el sistema
operativo lo exige, ve el indicador correspondiente (notificación
persistente de ubicación en segundo plano, punto verde de cámara/micrófono
si algún día se añadiera esa función, etc.). Esto no es una limitación
técnica que se pueda "resolver" ocultando el ícono: es un requisito de
Android/iOS pensado para que nadie pueda vigilar un dispositivo sin que su
usuario lo sepa, y es también la base legal y ética de este proyecto.

Ver [`ARCHITECTURE.md`](./ARCHITECTURE.md) para la propuesta técnica
completa (frontend, backend, base de datos, seguridad).

## Estructura del proyecto

```
control-parental/
├── ARCHITECTURE.md          Propuesta de arquitectura técnica completa
├── backend/                 API REST + WebSocket (NestJS + PostgreSQL)
│   ├── prisma/
│   │   └── schema.prisma    Esquema de base de datos
│   └── src/
│       ├── modules/
│       │   ├── auth/            Login, sesiones, vínculo familia-dispositivo
│       │   ├── location/        Ingesta y consulta de ubicación
│       │   ├── geofencing/      Zonas seguras (geocercas)
│       │   ├── screentime/      Límites de tiempo de pantalla por app
│       │   ├── alerts/          Botón de pánico / SOS
│       │   └── usage-reports/   Estadísticas de uso por categoría
│       └── common/
│           ├── encryption/      Cifrado en tránsito y en reposo
│           └── push/            Notificaciones push (FCM/APNs)
├── mobile-child/             App del menor (Android/Kotlin, Jetpack Compose)
│   └── .../child/
│       ├── location/         Servicio de ubicación en segundo plano
│       ├── geofencing/       Evaluación de geocercas en el dispositivo
│       ├── screentime/       Bloqueo de apps y límites de uso
│       ├── sos/              Botón de pánico
│       └── onboarding/       Explicación de permisos al menor
└── mobile-parent/            App del padre/tutor (Android/Kotlin, Compose)
    └── .../parent/
        ├── dashboard/        Panel de métricas y configuración
        ├── map/              Ubicación en tiempo real
        ├── geofencing/       Editor de zonas seguras
        └── alerts/           Recepción de alertas SOS y geocercas
```

## Por qué es "transparente" y no oculta

Este proyecto se diseñó explícitamente **sin** modo oculto, ícono
disfrazado, ni supresión de notificaciones del sistema, por tres razones,
en este orden de prioridad:

1. **Legal**: grabar pantalla/cámara o rastrear ubicación de forma
   encubierta puede constituir delito en la mayoría de jurisdicciones
   incluso siendo el padre/tutor, y las plataformas (Google Play Protect,
   Apple) tratan cualquier app que oculte su ícono o falsifique su estado
   de instalación como malware, con desinstalación forzosa y reporte.
2. **Técnico**: Android e iOS bloquean por diseño el acceso continuo a
   ubicación/cámara/pantalla sin notificación visible. Lograrlo de verdad
   requeriría explotar vulnerabilities o rootear/jailbrickear el
   dispositivo del menor, lo cual rompe cualquier garantía de seguridad
   del equipo.
3. **Eficacia real**: la evidencia en desarrollo adolescente muestra que
   la vigilancia encubierta, cuando se descubre (y casi siempre ocurre),
   destruye la confianza y empeora la comunicación, sin mejorar la
   seguridad a largo plazo. El control parental transparente es el
   estándar respaldado por Google Family Link, Apple Screen Time,
   Qustodio y Bark.

## Cómo correrlo (desarrollo)

```bash
# Backend
cd backend
npm install
npx prisma migrate dev
npm run start:dev

# Apps móviles: abrir mobile-parent/ y mobile-child/ como proyectos de
# Android Studio independientes (requieren SDK de Android, no incluido
# en este entorno — ver notas en ARCHITECTURE.md).
```
