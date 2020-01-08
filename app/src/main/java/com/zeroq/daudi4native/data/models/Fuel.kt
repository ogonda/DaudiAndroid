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
    var entries: List<TruckEntry>?
) {
    constructor() : this(0, null)
}

data class TruckEntry(var Name: String?, var Id: String?, var qty: Int?, var observed: Int?) {
    constructor() : this(null, null, null, null)
}