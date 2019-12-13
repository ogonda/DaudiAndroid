package com.zeroq.daudi4native.vo

import androidx.annotation.NonNull
import androidx.annotation.Nullable

class Resource<T> private constructor(
    @param:Nullable @field:Nullable
    private val data: T?, @param:Nullable @field:Nullable
    private val error: Exception?
) {

    val isSuccessful: Boolean
        get() = data != null && error == null

    constructor(@NonNull data: T) : this(data, null)

    constructor(@NonNull exception: Exception) : this(null, exception)

    @NonNull
    fun data(): T? {
        if (error != null) {
            throw IllegalStateException("error is not null. Call isSuccessful() first.")
        }
        return data
    }

    @NonNull
    fun error(): Exception? {
        if (data != null) {
            throw IllegalStateException("data is not null. Call isSuccessful() first.")
        }
        return error
    }
}
