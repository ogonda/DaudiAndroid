package com.zeroq.daudi4native.ui.dialogs.data

data class LoadingDialogEvent(
    var ikLoaded: Int?, var agoLoaded: Int?, var pmsLoaded: Int?,
    var sealRange: String?, var brokenSeal: String?, var DeliveryNumber: String?
)