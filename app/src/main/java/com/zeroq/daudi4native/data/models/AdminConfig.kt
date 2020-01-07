package com.zeroq.daudi4native.data.models

data class AdminConfig(
    var omcId: String?,
    var app: AppConfig?,
    var fcm: FcmObject?
) {
    constructor() : this(null, null, null)
}



