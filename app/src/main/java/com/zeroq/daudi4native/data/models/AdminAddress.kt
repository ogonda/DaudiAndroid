package com.zeroq.daudi4native.data.models


data class AdminAddress(
    var Id: String?,
    var Line1: String?,
    var City: String?,
    var CountrySubDivisionCode: String?,
    var PostalCode: String?
) {
    constructor() : this(null, null, null, null, null)
}