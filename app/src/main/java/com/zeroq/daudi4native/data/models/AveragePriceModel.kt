package com.zeroq.daudi4native.data.models

data class AveragePriceModel(
    var fueltytype: String?, var omcId: String?,
    var price: Double?, var user: AveragePriceUser?
) : Model() {
    constructor() : this(null, null, null, null)
}