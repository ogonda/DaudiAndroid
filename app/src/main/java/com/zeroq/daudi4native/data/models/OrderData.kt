package com.zeroq.daudi4native.data.models

data class OrderData(
    var OrderID: String?,
    var QbID: String?
) {
    constructor() : this(null, null)
}