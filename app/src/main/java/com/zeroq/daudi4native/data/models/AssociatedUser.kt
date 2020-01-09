package com.zeroq.daudi4native.data.models

import java.util.*

data class AssociatedUser(
    var name: String?,
    var adminId: String?,
    var date: Date?
) {
    constructor() : this(null, null, null)

}