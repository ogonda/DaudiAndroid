package com.zeroq.daudi4native.data.models


data class OrderModel(
    var Id: String?,
    var config: OrderConfig?,
    var customer: CustomerDetail?,
    var QbConfig: QbConfig?,
    var frozen: Boolean?,
    var seals: OrderSeals?,
    var printStatus: PrintingStatus?,
    var truckStageData: Map<String, TruckStageData>?,
    var truck: Truck?,
    var fuel: Fuel?,
    var deliveryNote: DeliveryNote?

) : Model() {
    constructor() : this(
        null, null, null,
        null, null, null, null,
        null, null, null, null
    )
}

data class DeliveryNote(var value: String?){
    constructor(): this(null)
}

data class QbConfig(var InvoiceId: String?) {
    constructor() : this(null)
}


data class CustomerDetail(
    var name: String?, var Id: String?,
    var contact: List<Contact>?
) {
    constructor() : this(null, null, null)
}

data class Contact(
    var name: String?,
    var phone: String?, var email: String?
) {
    constructor() : this(null, null, null)
}
