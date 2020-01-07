package com.zeroq.daudi4native.data.models


data class FcmObject(
    var tokens: FcmToken?
) {
    constructor() : this(null)
}