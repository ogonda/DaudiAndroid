package com.zeroq.daudi4native.data.models

data class UserModel(
    var Active: Boolean?, var Id: String?,
    var config: AdminConfig?,
    var profile: AdminProfile?,
    var dev: Boolean?,
    var email: String?,
    var status: UserStatus?
) : Model() {
    constructor() : this(
        null, null, null, null,
        null, null, null
    )
}