package com.kbk.domain.models.sdk

/**
 * Статистические данные для вычисления расстояния Махаланобиса
 * Хранит вектор средних значений и обратную ковариационную матрицу
 */
data class MahalanobisStats(
    val meanVector: DoubleArray,
    val inverseCovariance: Array<DoubleArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MahalanobisStats
        if (!meanVector.contentEquals(other.meanVector)) return false
        if (!inverseCovariance.contentDeepEquals(other.inverseCovariance)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = meanVector.contentHashCode()
        result = 31 * result + inverseCovariance.contentDeepHashCode()
        return result
    }
}
