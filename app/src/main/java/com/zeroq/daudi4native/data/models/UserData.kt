package com.zeroq.daudi4native.data.models

class UserData(var email: String?, var name: String?, var photoURL: String?, var uid: String?) {
    constructor() : this(null, null, null, null)
}