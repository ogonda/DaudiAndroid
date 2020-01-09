package com.zeroq.daudi4native.data.models

data class Truck(
    var stage: Int?,
    var compartmentCount: Int?,
    var hasBeenReset: Boolean?,
    var driverdetail: DriverDetail?,
    var truckdetail: TruckDetail?,
    var compartments: List<Compartment>?
) {
    constructor() : this(
        null, null, null,
        null, null, null
    )
}
