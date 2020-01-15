package com.zeroq.daudi4native.data.models

data class TruckStageData(var expiry: ArrayList<Expiry>?, var user: AssociatedUser?) {
    constructor() : this(ArrayList(), null)
}