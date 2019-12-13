package com.zeroq.daudi4native.events

import com.zeroq.daudi4native.data.models.TruckModel

class ProcessingEvent( val trucks: List<TruckModel>?, val error: Exception?)