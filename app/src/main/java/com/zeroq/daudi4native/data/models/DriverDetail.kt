package com.zeroq.daudi4native.data.models

data class DriverDetail(var name: String?, var id: String?, var phone: String?) {
    constructor() : this(null, null, null)
}