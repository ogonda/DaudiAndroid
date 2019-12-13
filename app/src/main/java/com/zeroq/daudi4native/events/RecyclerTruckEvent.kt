package com.zeroq.daudi4native.events

import com.zeroq.daudi4native.data.models.TruckModel

data class RecyclerTruckEvent(var position: Int, var truck: TruckModel)