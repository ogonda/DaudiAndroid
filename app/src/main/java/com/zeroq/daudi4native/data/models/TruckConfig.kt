package com.zeroq.daudi4native.data.models

data class TruckConfig(
    var companyid: String?,
    var depot: Depot?,
    var sandbox: Boolean?
) {
    constructor() : this(null, null, false)
}