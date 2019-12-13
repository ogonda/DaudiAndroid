package com.zeroq.daudi4native.data.models

import com.google.firebase.Timestamp

data class UserStatus(var online: Boolean?, var time: Timestamp?) {
    constructor() : this(false, null)
}