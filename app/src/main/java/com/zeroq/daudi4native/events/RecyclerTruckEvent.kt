package com.zeroq.daudi4native.events

import com.zeroq.daudi4native.data.models.OrderModel

data class RecyclerTruckEvent(var position: Int, var order: OrderModel)