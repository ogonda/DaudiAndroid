package com.zeroq.daudi4native.di.modules

import com.zeroq.daudi4native.di.fragment_modules.FragmentMainModules
import com.zeroq.daudi4native.ui.MainActivity
import com.zeroq.daudi4native.ui.activate.ActivateActivity
import com.zeroq.daudi4native.ui.average_prices.AveragePriceActivity
import com.zeroq.daudi4native.ui.device_list.DeviceListActivity
import com.zeroq.daudi4native.ui.loading_order.LoadingOrderActivity
import com.zeroq.daudi4native.ui.login.LoginActivity
import com.zeroq.daudi4native.ui.no_depot.NoDepot
import com.zeroq.daudi4native.ui.preview.PreviewActivity
import com.zeroq.daudi4native.ui.printing.PrintingActivity
import com.zeroq.daudi4native.ui.splash.SplashActivity
import com.zeroq.daudi4native.ui.truck_detail.TruckDetailActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Suppress("unused")
@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [FragmentMainModules::class])
    internal abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun bindSplashActivity(): SplashActivity

    @ContributesAndroidInjector
    internal abstract fun bindLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    internal abstract fun bindActivateActivity(): ActivateActivity


    @ContributesAndroidInjector
    internal abstract fun bindPrintingActivity(): PrintingActivity

    @ContributesAndroidInjector
    internal abstract fun bindTruckDetailActivity(): TruckDetailActivity

    @ContributesAndroidInjector
    internal abstract fun bindLoadingOrderActivity(): LoadingOrderActivity

    @ContributesAndroidInjector
    internal abstract fun bindAveragePriceActivity(): AveragePriceActivity


    @ContributesAndroidInjector
    internal abstract fun bindDeviceListActivity(): DeviceListActivity

    @ContributesAndroidInjector
    internal abstract fun bindPreviewActivity(): PreviewActivity

    @ContributesAndroidInjector
    internal abstract fun bindNoDepotActivity(): NoDepot
}