package com.zeroq.daudi4native.data.models

data class Printing(var status: Boolean?, var user: AssociatedUser?) {
    constructor() : this(null, null)
}
