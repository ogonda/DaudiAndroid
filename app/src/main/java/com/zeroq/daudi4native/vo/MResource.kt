package com.zeroq.daudi4native.vo

import com.zeroq.daudi4native.vo.Status.*


/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
data class MResource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): MResource<T> {
            return MResource(SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): MResource<T> {
            return MResource(ERROR, data, msg)
        }

        fun <T> loading(data: T?): MResource<T> {
            return MResource(LOADING, data, null)
        }
    }
}