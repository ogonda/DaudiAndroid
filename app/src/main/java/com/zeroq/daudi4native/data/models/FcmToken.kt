package com.zeroq.daudi4native.data.models

data class FcmToken(var apk: String?, var web: String?){
    constructor(): this(null, null)
}