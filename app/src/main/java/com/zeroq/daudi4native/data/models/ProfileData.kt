package com.zeroq.daudi4native.data.models

data class ProfileData(
    var address: String?, var bio: String?, var dob: String?,
    var gender: String?, var phone: String?
) {
    constructor() : this(null, null, null, null, null)
}