package com.zeroq.daudi4native.utils

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

class ReleaseTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        // push to firebase crash reporting
        Crashlytics.logException(t)
        Crashlytics.log(message)
    }
}