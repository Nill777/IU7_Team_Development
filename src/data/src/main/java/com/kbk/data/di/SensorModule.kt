package com.kbk.data.di

import android.content.Context
import com.kbk.data.sensors.AndroidMotionSensorTracker
import com.kbk.domain.irepository.IMotionSensorTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SensorModule {

    @Provides
    @Singleton
    fun provideMotionSensorTracker(
        @ApplicationContext context: Context
    ): IMotionSensorTracker {
        return AndroidMotionSensorTracker(context)
    }
}
