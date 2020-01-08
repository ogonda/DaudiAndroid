package com.zeroq.daudi4native.data.models

data class Compartment(
    var position: Int?,
    var fueltype: String?,
    var qty: Int?
) {
    constructor() : this(null,null, null)
}