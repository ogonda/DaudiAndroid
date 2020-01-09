package com.zeroq.daudi4native.events

import com.zeroq.daudi4native.data.models.OrderModel

class LoadingEvent(val orders: List<OrderModel>?, val error: Exception?)