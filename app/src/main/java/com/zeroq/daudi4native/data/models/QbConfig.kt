package com.zeroq.daudi4native.data.models

data class QbConfig(var QbId: String?, var companyid: String?, var sandbox: String?) {
    constructor() : this(null, null, null)
}