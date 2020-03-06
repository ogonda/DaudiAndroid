package com.zeroq.daudi4native.utils

import android.content.res.Resources
import android.util.TypedValue
import javax.inject.Inject


class Utils @Inject constructor() {

    fun stripNonDigits(input: CharSequence): Int {
        val sb: StringBuilder = StringBuilder(input.length)

        for (i in 0 until input.length) {
            val c = input[i]

            if (c > 47.toChar() && c < 58.toChar()) {
                sb.append(c)
            }
        }

        return sb.toString().toInt()
    }

    /**
     * Converting dp to pixel
     */
    fun dpToPx(dp: Int, r: Resources): Int {
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                r.getDisplayMetrics()
            )
        )
    }
}