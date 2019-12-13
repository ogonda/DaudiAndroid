package com.zeroq.daudi4native.data.models

import java.util.*

data class Stage(
    var user: _User?,
    var data: Data?
) {
    constructor() : this(null, null)
}

data class _User(
    var name: String?,
    var time: com.google.firebase.Timestamp?,
    var uid: String?
) {
    constructor() : this(null, null, null)
}


data class Seals(
    var range: String?,
    var broken: ArrayList<String>?
) {
    constructor() : this(null, null)
}

data class Data(
    // for stage 1,2,3
    var expiry: ArrayList<Expiry>?,

    // stage 4 alone
    var deliveryNote: String?,
    var seals: Seals?
) {
    constructor() : this(null, null, null)
}

