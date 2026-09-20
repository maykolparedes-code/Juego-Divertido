package com.maykol.juegodivertido

/**
 * IDs de anuncios y de la tienda en un solo lugar.
 *
 * Los IDs de anuncio de abajo son los IDs de PRUEBA públicos y oficiales de
 * Google (ver https://developers.google.com/admob/android/test-ads). Son
 * seguros para compilar y probar la app: siempre devuelven anuncios de
 * ejemplo y nunca generan ingresos ni arriesgan tu cuenta de AdMob.
 *
 * ANTES DE PUBLICAR: sustituye estos cuatro valores por los IDs reales que
 * genera tu propia cuenta de AdMob (ver README.md, sección "Configurar
 * AdMob"), y actualiza también el APPLICATION_ID en AndroidManifest.xml.
 */
object AdUnitIds {
    const val BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
}

/** Cada cuántas partidas terminadas se muestra un anuncio intersticial. */
const val INTERSTITIAL_FREQUENCY = 3
