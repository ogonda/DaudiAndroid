package com.zeroq.daudi4native.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class ReleaseTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        // push to firebase crash reporting

        t?.let { FirebaseCrashlytics.getInstance().recordException(it) }
        FirebaseCrashlytics.getInstance().log(message)
    }
}