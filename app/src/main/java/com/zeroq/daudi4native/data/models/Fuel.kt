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

data class TruckEntry(var name: String?, var id: String?, var qty: Int?, var observed: Int?) {
    constructor() : this(null, null, null, null)
}

data class PriceConfig(
    var requestedPrice: Int?,
    var price: Int?,
    var nonTaxprice: Int?,
    var nonTax: Int?,
    var retailprice: Int?,
    var minsp: Int?,
    var total: Int?,
    var taxAmnt: Int?,
    var nonTaxtotal: Int?,
    var taxablePrice: Int?,
    var taxableAmnt: Int?,
    var difference: Int?
) {
    constructor() : this(null, null,
        null, null, null,
        null, null, null, null,
        null, null, null)
}