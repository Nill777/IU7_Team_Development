package com.kbk.keystrokeSdk.math

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Легковесный математический движок для линейной алгебры
 */
internal object MatrixMath {

    private const val RIDGE_LAMBDA = 1e-6
    private const val ZERO_THRESHOLD = 1e-12

    fun mean(data: List<DoubleArray>): DoubleArray {
        val dimensions = data.first().size
        val result = DoubleArray(dimensions)
        for (vec in data) {
            for (i in 0 until dimensions) {
                result[i] += vec[i]
            }
        }
        for (i in 0 until dimensions) {
            result[i] /= data.size.toDouble()
        }
        return result
    }

    fun covariance(data: List<DoubleArray>, mean: DoubleArray): Array<DoubleArray> {
        val dimensions = mean.size
        val cov = Array(dimensions) { DoubleArray(dimensions) }
        val n = data.size.toDouble()

        for (vec in data) {
            for (i in 0 until dimensions) {
                for (j in 0 until dimensions) {
                    cov[i][j] += (vec[i] - mean[i]) * (vec[j] - mean[j])
                }
            }
        }
        for (i in 0 until dimensions) {
            for (j in 0 until dimensions) {
                cov[i][j] /= (n - 1.0) // Несмещенная оценка
            }
        }
        return cov
    }

    /**
     * Инверсия матрицы методом Гаусса-Жордана с L2-регуляризацией
     */
    fun invert(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val n = matrix.size
        val augmented = createAugmentedMatrix(matrix, n)
        performGaussianElimination(augmented, n)
        return extractInverseMatrix(augmented, n)
    }

    fun mahalanobis(x: DoubleArray, mean: DoubleArray, invCov: Array<DoubleArray>): Double {
        val n = x.size
        val diff = DoubleArray(n) { x[it] - mean[it] }

        var distSq = 0.0
        for (i in 0 until n) {
            var temp = 0.0
            for (j in 0 until n) {
                temp += diff[j] * invCov[i][j]
            }
            distSq += temp * diff[i]
        }
        return sqrt(abs(distSq))
    }

    private fun createAugmentedMatrix(matrix: Array<DoubleArray>, n: Int): Array<DoubleArray> {
        val augmented = Array(n) { DoubleArray(2 * n) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                // Добавляем RIDGE_LAMBDA на диагональ для защиты от вырождения
                val regularization = if (i == j) RIDGE_LAMBDA else 0.0
                augmented[i][j] = matrix[i][j] + regularization
            }
            augmented[i][i + n] = 1.0
        }
        return augmented
    }

    private fun performGaussianElimination(augmented: Array<DoubleArray>, n: Int) {
        for (i in 0 until n) {
            var pivotRow = i
            for (j in i + 1 until n) {
                if (abs(augmented[j][i]) > abs(augmented[pivotRow][i])) {
                    pivotRow = j
                }
            }

            val temp = augmented[i]
            augmented[i] = augmented[pivotRow]
            augmented[pivotRow] = temp

            val pivotVal = augmented[i][i]
            if (abs(pivotVal) < ZERO_THRESHOLD) {
                throw IllegalArgumentException("Матрица вырождена. Недостаточно уникальных данных.")
            }

            for (j in 0 until 2 * n) {
                augmented[i][j] /= pivotVal
            }

            for (j in 0 until n) {
                if (i != j) {
                    val factor = augmented[j][i]
                    for (k in 0 until 2 * n) {
                        augmented[j][k] -= factor * augmented[i][k]
                    }
                }
            }
        }
    }

    private fun extractInverseMatrix(augmented: Array<DoubleArray>, n: Int): Array<DoubleArray> {
        val inverse = Array(n) { DoubleArray(n) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                inverse[i][j] = augmented[i][j + n]
            }
        }
        return inverse
    }
}
