package com.kbk.keystrokeSdk.ensemble

import com.kbk.domain.isdk.IVerificationModel
import com.kbk.domain.isdk.VerificationStrategy
import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.sdk.BiometricProfile
import com.kbk.domain.models.sdk.ModelProfile
import com.kbk.domain.models.sdk.VerificationResult
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KeystrokeVerificationManagerTest {
    private lateinit var mockTiming: IVerificationModel
    private lateinit var mockSpatial: IVerificationModel
    private lateinit var mockMotion: IVerificationModel
    private lateinit var manager: KeystrokeVerificationManager

    @Before
    fun setup() {
        mockTiming = mockk()
        mockSpatial = mockk()
        mockMotion = mockk()

        every { mockTiming.modelName } returns "TimingModel"
        every { mockSpatial.modelName } returns "SpatialModel"
        every { mockMotion.modelName } returns "MotionModel"

        manager = KeystrokeVerificationManager(mockTiming, mockSpatial, mockMotion)
    }

    @Test
    fun `verify ensemble should return true if majority votes true`() {
        val emptyAttempt = emptyList<BiometricSample>()
        val profile = BiometricProfile(
            modelProfiles = mapOf(
                "TimingModel" to ModelProfile("TimingModel"),
                "SpatialModel" to ModelProfile("SpatialModel"),
                "MotionModel" to ModelProfile("MotionModel")
            )
        )
        val thresholds = emptyMap<String, Float>()

        // голосование: 2 ЗА, 1 ПРОТИВ
        every { mockTiming.verify(any(), any(), any()) } returns VerificationResult(
            "TimingModel",
            true,
            1f,
            1f
        )
        every { mockSpatial.verify(any(), any(), any()) } returns VerificationResult(
            "SpatialModel",
            true,
            1f,
            1f
        )
        every { mockMotion.verify(any(), any(), any()) } returns VerificationResult(
            "MotionModel",
            false,
            10f,
            1f
        )

        val results = manager.verify(emptyAttempt, profile, thresholds)
        val ensembleResult = results.last()

        assertEquals("Ensemble", ensembleResult.modelName)
        assertTrue(
            "Ансамбль должен пропустить, так как 2 из 3 моделей ЗА",
            ensembleResult.isOwner
        )
    }

    @Test
    fun `verify ensemble should return false if majority votes false`() {
        val emptyAttempt = emptyList<BiometricSample>()
        val profile = BiometricProfile(
            modelProfiles = mapOf(
                "TimingModel" to ModelProfile("TimingModel"),
                "SpatialModel" to ModelProfile("SpatialModel"),
                "MotionModel" to ModelProfile("MotionModel")
            )
        )

        // голосование: 1 ЗА, 2 ПРОТИВ
        every { mockTiming.verify(any(), any(), any()) } returns VerificationResult(
            "TimingModel",
            true,
            1f,
            1f
        )
        every { mockSpatial.verify(any(), any(), any()) } returns VerificationResult(
            "SpatialModel",
            false,
            10f,
            1f
        )
        every { mockMotion.verify(any(), any(), any()) } returns VerificationResult(
            "MotionModel",
            false,
            10f,
            1f
        )

        val results = manager.verify(emptyAttempt, profile, emptyMap())
        val ensembleResult = results.last()

        assertFalse(
            "Ансамбль должен отклонить, так как 2 из 3 моделей ПРОТИВ",
            ensembleResult.isOwner
        )
    }

    @Test
    fun `verify with TIMING_ONLY strategy should use only timing model`() {
        manager.setStrategy(VerificationStrategy.TIMING_ONLY)

        val profile =
            BiometricProfile(modelProfiles = mapOf("TimingModel" to ModelProfile("TimingModel")))
        every { mockTiming.verify(any(), any(), any()) } returns VerificationResult(
            "TimingModel",
            true,
            1f,
            1f
        )

        val results = manager.verify(emptyList(), profile, emptyMap())

        assertEquals("Только один результат должен вернуться", 1, results.size)
        assertEquals("TimingModel", results.first().modelName)
    }
}
