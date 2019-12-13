package com.zeroq.daudi4native.data.models

data class BatchModel(var accumulated: BatchAccumulated?, var status: Int?) {
    constructor() : this(null, null)
}


data class BatchAccumulated(var total: Int?, var usable: Int?) {
    constructor() : this(null, null)
}