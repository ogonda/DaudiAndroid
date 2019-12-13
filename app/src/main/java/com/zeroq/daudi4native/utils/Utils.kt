package com.zeroq.daudi4native.utils

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
}