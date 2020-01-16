package com.zeroq.daudi4native.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import com.zeroq.daudi4native.data.models.*
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.DocumentLiveData
import com.zeroq.daudi4native.vo.QueryLiveData
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class OmcRepository @Inject constructor(
    @Named("omc") val omc: CollectionReference,
    val firestore: FirebaseFirestore,
    var firebaseAuth: FirebaseAuth
) {


    fun getAllOmcs(): QueryLiveData<OmcModel> {
        return QueryLiveData(omc, OmcModel::class.java)
    }

    /*
     * get all orders
     * from stage 1 t0 3
    */
    fun getOrders(user: UserModel): QueryLiveData<OrderModel> {
        return QueryLiveData(ordersQuery(user), OrderModel::class.java)
    }

    fun getOrder(combinedUserOrderId: Pair<UserModel, String>): DocumentLiveData<OrderModel> {
        val user: UserModel = combinedUserOrderId.first
        val orderId: String = combinedUserOrderId.second

        val orderRef = omc.document(user.config?.omcId!!)
            .collection("orders")
            .document(orderId)

        val data: DocumentLiveData<OrderModel> = DocumentLiveData(orderRef, OrderModel::class.java)
        orderRef.addSnapshotListener(data)

        return data
    }

    private fun ordersQuery(user: UserModel): Query {
        return omc
            .document(user.config?.omcId!!)
            .collection("orders")
            .whereEqualTo("config.depot.id", user.config?.app?.depotid!!)
            .whereGreaterThan("truck.stage", 0)
            .whereLessThan("truck.stage", 4)
    }

    /*
    * update processing expire time
    * */
    fun updateProcessingExpire(
        user: UserModel,
        order: OrderModel,
        minutes: Long
    ): CompletionLiveData {
        val completion = CompletionLiveData()
        updateProcessingExpireTask(user, order, minutes).addOnCompleteListener(completion)

        return completion
    }


    private fun updateProcessingExpireTask(
        user: UserModel,
        orderData: OrderModel,
        minutes: Long
    ): Task<Void> {


        /*
        * omc id will be from the user config for more security
        * */
        val orderRef = omc.document(user.config?.omcId!!)
            .collection("orders")
            .document(orderData.Id!!)


        return firestore.runTransaction { transaction: Transaction ->
            val order: OrderModel? = transaction.get(orderRef).toObject(OrderModel::class.java)

            // add new time
            val startDate = Calendar.getInstance().time

            val calendar = Calendar.getInstance()
            calendar.time = startDate
            calendar.add(Calendar.MINUTE, minutes.toInt())

            val expireDate = calendar.time

            /**
             * modify the truck object
             * */
            val expireObj = Expiry(startDate, expireDate)

            val exp: ArrayList<Expiry>? = order?.truckStageData!!["1"]?.expiry
            exp?.add(0, expireObj)

            // commit to fireStore
            transaction.update(orderRef, "truckStageData.1.expiry", exp)


            firebaseAuth.currentUser?.let {
                val printedBy = AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                // commit current user
                transaction.update(orderRef, "truckStageData.1.user", printedBy)
            }

            return@runTransaction null
        }

    }
}