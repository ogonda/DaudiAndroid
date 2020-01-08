package com.zeroq.daudi4native.data.models

data class PrintingStatus(var LoadingOrder: Printing?, var gatepass: Printing?) {
    constructor() : this(null, null)
}
