package com.maykol.controlfamiliar.child.network

/**
 * Mapea el nombre de paquete de una app a una categoría para los reportes
 * de uso. En un proyecto real esto vendría de un catálogo editable por el
 * padre (o de la API de Play Store); esta tabla mínima cubre los casos
 * más comunes y usa "OTHER" para todo lo demás.
 */
object AppCategoryMapper {
    private val knownPackages = mapOf(
        "com.tiktok" to "SOCIAL",
        "com.instagram.android" to "SOCIAL",
        "com.whatsapp" to "SOCIAL",
        "com.google.android.youtube" to "ENTERTAINMENT",
        "com.netflix.mediaclient" to "ENTERTAINMENT",
        "com.duolingo" to "EDUCATION",
        "com.khanacademy.android" to "EDUCATION",
        "com.google.android.apps.docs" to "PRODUCTIVITY",
    )

    private val gamePackagePrefixes = listOf("com.king.", "com.supercell.", "com.mojang.")

    fun categoryFor(packageName: String): String {
        knownPackages[packageName]?.let { return it }
        if (gamePackagePrefixes.any { packageName.startsWith(it) }) return "GAMES"
        return "OTHER"
    }
}
