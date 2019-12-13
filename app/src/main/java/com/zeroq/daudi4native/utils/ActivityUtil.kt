package com.zeroq.daudi4native.utils

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import javax.inject.Inject

class ActivityUtil @Inject constructor() {
    /***
     * This module will contain all most of the views automation
     * */

    fun showProgress(view: View, show: Boolean) {
        if (show) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    fun showTextViewState(view: TextView, show: Boolean, msg: String?, tvColor: Int?) {
        if (show) {
            view.setTextColor(tvColor!!)
            view.visibility = View.VISIBLE
            view.text = msg
        } else {
            view.visibility = View.GONE
        }
    }


    fun disableViews(layout: ViewGroup) {
        layout.isEnabled = false

        for (i in 0 until layout.childCount) {
            val child: View = layout.getChildAt(i)

            if (child is ViewGroup) {
                disableViews(child)
            } else {
                if (child is EditText || child is AppCompatButton) {
                    child.isEnabled = false
                }
            }
        }
    }

    fun totalDisableViews(layout: ViewGroup) {
        layout.isEnabled = false

        for (i in 0 until layout.childCount) {
            val child: View = layout.getChildAt(i)

            if (child is ViewGroup) {
                disableViews(child)
            } else {
                child.isEnabled = false
            }
        }
    }


    fun enableViews(layout: ViewGroup) {
        layout.isEnabled = true

        for (i in 0 until layout.childCount) {
            val child: View = layout.getChildAt(i)

            if (child is ViewGroup) {
                enableViews(child)
            } else {
                child.isEnabled = true
            }
        }
    }

}