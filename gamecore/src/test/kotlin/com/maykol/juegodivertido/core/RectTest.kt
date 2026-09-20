package com.maykol.juegodivertido.core

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RectTest {

    @Test
    fun `overlapping rects intersect`() {
        val a = Rect(0f, 0f, 1f, 1f)
        val b = Rect(0.5f, 0.5f, 1f, 1f)
        assertTrue(a.intersects(b))
        assertTrue(b.intersects(a))
    }

    @Test
    fun `separated rects do not intersect`() {
        val a = Rect(0f, 0f, 1f, 1f)
        val b = Rect(5f, 0f, 1f, 1f)
        assertFalse(a.intersects(b))
    }

    @Test
    fun `touching edges do not count as intersecting`() {
        val a = Rect(0f, 0f, 1f, 1f)
        val b = Rect(1f, 0f, 1f, 1f)
        assertFalse(a.intersects(b))
    }
}
