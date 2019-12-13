package com.zeroq.daudi4native.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zeroq.daudi4native.di.qualifires.ViewModelKey
import com.zeroq.daudi4native.ui.average_prices.AverageViewModel
import com.zeroq.daudi4native.ui.loading.LoadingViewModel
import com.zeroq.daudi4native.ui.loading_order.LoadingOrderViewModel
import com.zeroq.daudi4native.ui.login.LoginViewModel
import com.zeroq.daudi4native.ui.main.MainViewModel
import com.zeroq.daudi4native.ui.printing.PrintingViewModel
import com.zeroq.daudi4native.ui.processing.ProcessingViewModel
import com.zeroq.daudi4native.ui.queued.QueuedViewModel
import com.zeroq.daudi4native.ui.splash.SplashViewModel
import com.zeroq.daudi4native.ui.truck_detail.TruckDetailViewModel
import com.zeroq.daudi4native.viewmodel.DaudiViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Suppress("unused")
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    abstract fun bindSplashViewModel(splashViewModel: SplashViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(ProcessingViewModel::class)
    abstract fun bindProcessingViewModel(processingViewModel: ProcessingViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(TruckDetailViewModel::class)
    abstract fun bindTruckDetailViewModel(truckDetailViewModel: TruckDetailViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(PrintingViewModel::class)
    abstract fun bindPrintingViewModel(printingViewModel: PrintingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QueuedViewModel::class)
    abstract fun bindQueuedViewModel(queuedViewModel: QueuedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoadingViewModel::class)
    abstract fun bindLoadingViewModel(loadingViewModel: LoadingViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(LoadingOrderViewModel::class)
    abstract fun bindLoadingOrderViewModel(loadingOrderViewModel: LoadingOrderViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AverageViewModel::class)
    abstract fun bind(averageViewModel: AverageViewModel): ViewModel


    @Binds
    abstract fun bindViewModelFactory(factory: DaudiViewModelFactory): ViewModelProvider.Factory
}