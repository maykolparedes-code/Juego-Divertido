# Reglas de ProGuard/R8 para builds de release.
# Las reglas por defecto de Android ya ofuscan y reducen el código de forma
# segura; aquí solo protegemos lo que rompería a las librerías de anuncios
# y compras si se ofuscara.

# Google Mobile Ads (AdMob) publica sus propias consumer-rules, pero
# reforzamos por si alguna versión no las trae empaquetadas.
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }

# Play Billing Library: mantiene sus modelos serializables intactos.
-keep class com.android.billingclient.api.** { *; }

# Nuestros modelos de datos persistidos/serializados (DataStore) no deben
# perder nombres de campos.
-keepclassmembers class com.maykol.juegodivertido.data.** {
    <fields>;
}

# Elimina logs de depuración en builds de release para no filtrar información
# en logcat de dispositivos de usuarios finales.
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}
