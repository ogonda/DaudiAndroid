package com.zeroq.daudi4native.data.models


data class TruckModel(
    var Id: String?,
    var truckId: String?,
    var numberplate: String?,
    var stage: Int?,
    var isPrinted: Boolean?,
    var isprinted: Boolean?,
    var drivername: String?,
    var driverid: String?,
    var frozen: Boolean?,
    var company: Company?,
    var compartments: List<Compartment>?,
    var orderdata: OrderData?,
    var config: TruckConfig?,
    var fuel: Fuel?,
    var stagedata: Map<String, Stage>?,
    var beforeTesting: Boolean?
): Model() {
    constructor()
            : this(
        null, null,
        null, null, false,
        false, null, null,
        null, null, null,
        null, null, null, null,
        false
    )
}