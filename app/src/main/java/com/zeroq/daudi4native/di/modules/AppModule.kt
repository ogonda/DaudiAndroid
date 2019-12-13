package com.zeroq.daudi4native.di.modules

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import org.greenrobot.eventbus.EventBus
import javax.inject.Singleton

/**
 * This will expose class modules
 * eg, network, storageModule, etc
 * **/
@Module(
    includes = [
        ViewModelModule::class,
        AuthModule::class,
        FirestoreModule::class,
        BroadCastModule::class
    ]
)
class AppModule {

    @Provides
    @Singleton
    fun providesEventBus(): EventBus {
        return EventBus.getDefault()
    }

    @Provides
    @Singleton
    fun providesAlarmManager(app: Application): AlarmManager {
        return app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
}