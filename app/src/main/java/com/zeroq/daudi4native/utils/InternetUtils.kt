package com.zeroq.daudi4native.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import io.reactivex.Observable
import javax.inject.Inject


class InternetUtils @Inject constructor() {

    fun isInternetOn(context: Context): Observable<Boolean> {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return Observable.just(activeNetworkInfo != null && activeNetworkInfo.isConnected)
    }
}