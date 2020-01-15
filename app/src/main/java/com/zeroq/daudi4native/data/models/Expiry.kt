package com.zeroq.daudi4native.data.models
import java.util.*


data class Expiry(
    var timeCreated: Date?,
    var expiry: Date?
) {
    constructor() : this(null, null)
}
