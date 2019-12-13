package com.zeroq.daudi4native.data.models

data class Batch(
    var Id: String?,
    var Name: String?,
    // String or Integer
    var observed: Any?,
    var qty: Int?
) {
    constructor() : this(null, null, null, null)
}