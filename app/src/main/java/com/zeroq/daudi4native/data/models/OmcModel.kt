package com.zeroq.daudi4native.data.models

data class OmcModel(var license: String?, var name: String?) : Model() {
    constructor() : this(null, null)
}