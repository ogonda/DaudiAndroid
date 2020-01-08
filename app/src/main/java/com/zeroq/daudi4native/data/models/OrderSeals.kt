package com.zeroq.daudi4native.data.models

data class OrderSeals(var broken: List<String>?, var range: List<String>?) {
    constructor() : this(ArrayList<String>(), ArrayList<String>())
}