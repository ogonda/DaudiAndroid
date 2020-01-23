package com.zeroq.daudi4native.data.models

data class OrderSeals(var broken: ArrayList<String>?, var range: ArrayList<String>?) {
    constructor() : this(ArrayList<String>(), ArrayList<String>())
}