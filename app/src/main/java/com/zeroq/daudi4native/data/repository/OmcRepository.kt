package com.zeroq.daudi4native.data.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zeroq.daudi4native.data.models.OmcModel
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.vo.QueryLiveData
import javax.inject.Inject
import javax.inject.Named

class OmcRepository @Inject constructor(
    @Named("omc") val omc: CollectionReference,
    val firestore: FirebaseFirestore
) {


    fun getAllOmcs(): QueryLiveData<OmcModel> {
        return QueryLiveData(omc, OmcModel::class.java)
    }

    /*
     * get all orders
     * from stage 1 t0 3
    */
    fun getOrders(user: UserModel): QueryLiveData<OrderModel> {
        return QueryLiveData(ordersQuery(user), OrderModel::class.java);
    }


    private fun ordersQuery(user: UserModel): Query {
        return omc
            .document(user.config?.omcId!!)
            .collection("orders")
            .whereEqualTo("config.depot.id", user.config?.app?.depotid!!)
            .whereGreaterThan("stage", 0)
            .whereLessThan("stage", 4)
    }


}