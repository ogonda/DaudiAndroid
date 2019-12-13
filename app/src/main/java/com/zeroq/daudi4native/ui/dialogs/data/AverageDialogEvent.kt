package com.zeroq.daudi4native.ui.dialogs.data

import com.zeroq.daudi4native.data.models.OmcModel

data class AverageDialogEvent(
    var pms: Double?, var ago: Double?,
    var ik: Double?,
    var omc: OmcModel?
) {
    constructor() : this(null, null, null, null)
}