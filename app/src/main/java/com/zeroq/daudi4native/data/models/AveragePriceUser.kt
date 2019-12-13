package com.zeroq.daudi4native.data.models

import java.util.*

data class AveragePriceUser(var name: String?, var time: Date?, var uid: String?) {
    constructor() : this(null, null, null)
}