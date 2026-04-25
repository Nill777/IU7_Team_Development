package com.kbk.keystrokeSdk.math

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class MatrixMathTest {
    private val delta = 1e-5

    @Test
    fun `mean should calculate correct average vector`() {
        val data = listOf(
            doubleArrayOf(1.0, 2.0),
            doubleArrayOf(3.0, 4.0),
            doubleArrayOf(5.0, 6.0)
        )
        val expected = doubleArrayOf(3.0, 4.0)
        val actual = MatrixMath.mean(data)

        assertArrayEquals(expected, actual, delta)
    }

    @Test
    fun `invert should correctly invert 2x2 matrix`() {
        // Матрица: [[4.0, 7.0],[2.0, 6.0]]
        // Определитель = 24 - 14 = 10
        val matrix = arrayOf(
            doubleArrayOf(4.0, 7.0),
            doubleArrayOf(2.0, 6.0)
        )

        // Ожидаемая обратная: [[0.6, -0.7], [-0.2, 0.4]]
        val expected = arrayOf(
            doubleArrayOf(0.6, -0.7),
            doubleArrayOf(-0.2, 0.4)
        )

        val actual = MatrixMath.invert(matrix)

        assertArrayEquals(expected[0], actual[0], delta)
        assertArrayEquals(expected[1], actual[1], delta)
    }

    @Test
    fun `mahalanobis should calculate correct distance`() {
        val x = doubleArrayOf(2.0, 2.0)
        val mean = doubleArrayOf(0.0, 0.0)
        // Единичная матрица (расстояние Махаланобиса должно быть равно Евклидову)
        val invCov = arrayOf(
            doubleArrayOf(1.0, 0.0),
            doubleArrayOf(0.0, 1.0)
        )

        val distance = MatrixMath.mahalanobis(x, mean, invCov)

        // Евклидово расстояние для (2,2) от (0,0) = sqrt(2^2 + 2^2) = sqrt(8)
        assertEquals(sqrt(8.0), distance, delta)
    }
}
