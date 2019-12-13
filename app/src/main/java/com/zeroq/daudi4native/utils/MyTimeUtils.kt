package com.zeroq.daudi4native.utils

import java.util.concurrent.TimeUnit

class MyTimeUtils {
    companion object {
        fun formatElapsedTime(millis: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

            return when {
                hours == 0L && minutes == 0L -> String.format("00:00:%02d", seconds)

                hours == 0L && minutes > 0L -> String.format("00:%02d:%02d", minutes, seconds)

                else -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        }
    }
}