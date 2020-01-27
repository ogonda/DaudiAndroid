package com.zeroq.daudi4native.data.models

data class Fuel(
    var ago: Batches?,
    var ik: Batches?,
    var pms: Batches?
) {
    constructor() : this(null, null, null)
}

data class Batches(
    var qty: Int?,
    var entries: List<TruckEntry>?,
    var entryIds: ArrayList<String>?,
    var priceconfig: PriceConfig?

) {
    constructor() : this(0, null, null, null)
}

data class TruckEntry(var Name: String?, var Id: String?, var qty: Int?, var observed: Int?) {
    constructor() : this(null, null, null, null)
}

data class PriceConfig(
    var requestedPrice: Number?,
    var price: Number?,
    var nonTaxprice: Number?,
    var nonTax: Number?,
    var retailprice: Number?,
    var minsp: Number?,
    var total: Number?,
    var taxAmnt: Number?,
    var nonTaxtotal: Number?,
    var taxablePrice: Number?,
    var taxableAmnt: Number?,
    var difference: Number?
) {
    constructor() : this(null, null,
        null, null, null,
        null, null, null, null,
        null, null, null)
}