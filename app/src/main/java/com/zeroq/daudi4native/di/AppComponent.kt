package com.zeroq.daudi4native.di

import android.app.Application
import com.zeroq.daudi4native.DaudiApplication
import com.zeroq.daudi4native.di.modules.ActivityBuilder
import com.zeroq.daudi4native.di.modules.AppModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        ActivityBuilder::class
    ]
)

interface AppComponent : AndroidInjector<DaudiApplication> {

    fun inject(app: Application)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    override fun inject(app: DaudiApplication)
}