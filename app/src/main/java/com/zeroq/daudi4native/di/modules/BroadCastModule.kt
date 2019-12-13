package com.zeroq.daudi4native.di.modules

import com.zeroq.daudi4native.broadcasts.TruckExpireBroadCast
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class BroadCastModule {

    @ContributesAndroidInjector
    abstract fun contributeTruckExpireBroadCast(): TruckExpireBroadCast
}