package com.maykol.controlfamiliar.child.location

/**
 * Cola en memoria para pings que no se pudieron enviar por falta de red.
 * Suficiente para esta demo; en producción esto debería persistirse (Room)
 * y reintentarse con WorkManager cuando vuelva la conectividad, para no
 * perder los pings si la app se cierra antes de poder reenviarlos.
 */
object PendingLocationQueue {
    private val pending = mutableListOf<Triple<Double, Double, Float>>()

    @Synchronized
    fun enqueue(lat: Double, lng: Double, accuracyMeters: Float) {
        pending.add(Triple(lat, lng, accuracyMeters))
    }

    @Synchronized
    fun drain(): List<Triple<Double, Double, Float>> {
        val copy = pending.toList()
        pending.clear()
        return copy
    }
}
