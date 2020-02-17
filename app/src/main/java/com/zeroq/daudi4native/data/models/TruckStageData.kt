package com.zeroq.daudi4native.data.models

data class TruckStageData(
    var expiry: ArrayList<Expiry>?, var user: AssociatedUser?,
    var totalExpiredTime: Long?, var totalApproxTime: Long?, var Additions: Int?
) {
    constructor() : this(
        ArrayList(),
        null,
        0,
        0, 0
    )
}