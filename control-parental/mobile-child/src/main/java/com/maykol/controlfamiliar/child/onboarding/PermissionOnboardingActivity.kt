package com.maykol.controlfamiliar.child.onboarding

/**
 * Pantalla mostrada antes de solicitar cada permiso sensible (ubicación en
 * segundo plano, accesibilidad, uso de apps). Es texto adicional AL de los
 * diálogos del sistema operativo, nunca un reemplazo, y explica en
 * lenguaje simple qué hace cada permiso:
 *
 *  - "Ubicación en segundo plano": tu familia puede ver dónde estás,
 *    incluso con la app cerrada. Verás un aviso fijo en tu barra de
 *    notificaciones mientras esto esté activo.
 *  - "Accesibilidad": permite que la app sepa qué aplicación tienes
 *    abierta, para avisarte cuando se acabe el tiempo permitido.
 *  - "Uso de aplicaciones": permite generar el reporte de cuánto tiempo
 *    pasas en cada app.
 *
 * Esta misma pantalla es accesible en cualquier momento desde el menú
 * principal de la app ("Qué comparte esta app"), para que el menor pueda
 * revisarla cuando quiera, no solo durante la instalación.
 *
 * (Composable de UI omitido por brevedad en este esqueleto — reemplazar
 * por las pantallas reales de Jetpack Compose del proyecto.)
 */
class PermissionOnboardingActivity
