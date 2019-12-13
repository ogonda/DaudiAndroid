package com.zeroq.daudi4native.data.models

data class Compartment(
    var fueltype: String?,
    var qty: Int?
) {
    constructor() : this(null, null)
}