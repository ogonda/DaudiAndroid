package com.zeroq.daudi4native.data.models

import java.util.*

data class Expiry(
    var startTime: Any?,
    var time: String?,
    var timestamp: Date?
) {
    constructor() : this(null, null, null)
}
