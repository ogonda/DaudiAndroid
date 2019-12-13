package com.zeroq.daudi4native.data.models

data class Config(
    var depotid: String?,
    var viewsandbox: Boolean?
) {
    constructor() : this(null, false)
}