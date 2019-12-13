package com.zeroq.daudi4native.commons

import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import dagger.android.support.DaggerAppCompatActivity
import android.annotation.SuppressLint


@SuppressLint("Registered")
open class BaseActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected fun <T : ViewModel> getViewModel(cls: Class<T>): T {
        return ViewModelProviders.of(this, viewModelFactory).get(cls)
    }

}