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
    var batches: Map<String, Batch>?
) {
    constructor() : this(0, null)
}