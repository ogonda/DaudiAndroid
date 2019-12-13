package com.zeroq.daudi4native.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zeroq.daudi4native.ui.MainActivity
import com.zeroq.daudi4native.utils.TruckNotification
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class TruckExpireBroadCast : BroadcastReceiver() {

    @Inject
    lateinit var truckNotification: TruckNotification

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context)

        Timber.d("All is okay")

        if (intent!!.hasExtra("CONTENT")) {
            val cont = intent.getStringExtra("CONTENT")
            val title = intent.getStringExtra("TITLE")
            val requestCode = intent.getIntExtra("REQUEST_CODE", 100)

            truckNotification.showNotification(
                context!!,
                MainActivity::class.java, title, cont,
                requestCode
            )
        }

    }
}