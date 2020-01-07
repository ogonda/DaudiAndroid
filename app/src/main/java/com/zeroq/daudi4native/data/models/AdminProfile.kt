package com.zeroq.daudi4native.data.models

data class AdminProfile(
    var email: String?,
    var uid: String?,
    var photoUrl: String?,
    var name: String?,
    var gender: String?,
    var dob: String?,
    var bio: String?,
    var phone: String?,

    var address: AdminAddress?
) {
    constructor() : this(
        null, null, null, null, null, null, null,
        null, null
    )
}

