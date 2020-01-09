package com.zeroq.daudi4native.events

import com.zeroq.daudi4native.data.models.OrderModel

class QueueingEvent(val orders: List<OrderModel>?, val error: Exception?)