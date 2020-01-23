package com.zeroq.daudi4native.data.models

data class EntryModel(var qty: DirectLoad?, var total: Int?, var used: Int?){
    constructor(): this(null, null, null)
}


data class DirectLoad(var total: Int?, var accumulated: Accumulated?){
    constructor(): this(null, null)
}

data class Accumulated(var total: Int?, var usable: Int?){
    constructor(): this(null, null)
}