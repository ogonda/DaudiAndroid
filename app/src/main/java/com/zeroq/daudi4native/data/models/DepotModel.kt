package com.zeroq.daudi4native.data.models

data class DepotModel(var Name: String?, var config: ConfigDepot?) : Model() {
    constructor() : this(null, null)
}

data class ConfigDepot(var private: Boolean?) {
    constructor() : this(false)
}