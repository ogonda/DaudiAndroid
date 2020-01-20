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
import java.lang.Void as Void

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
             *
             * set expirely and the user
             * */
            firebaseAuth.currentUser?.let {
                val setbyExpire =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val expireObj = Expiry(startDate, expireDate, setbyExpire)

                val exp: ArrayList<Expiry>? = order?.truckStageData!!["1"]?.expiry
                exp?.add(0, expireObj)

                // commit to fireStore
                transaction.update(orderRef, "truckStageData.1.expiry", exp)
            }

            return@runTransaction null
        }

    }


    fun updateOrderDetails(
        user: UserModel,
        orderId: String,
        compartmentList: List<Compartment>,
        driverId: String,
        driverName: String,
        numberPlate: String
    ): CompletionLiveData {
        val completion = CompletionLiveData()
        updateOrderDetailsTask(
            user,
            orderId,
            compartmentList,
            driverId,
            driverName,
            numberPlate
        ).addOnCompleteListener(completion)

        return completion
    }

    private fun updateOrderDetailsTask(
        user: UserModel,
        orderId: String,
        compartmentList: List<Compartment>,
        driverId: String,
        driverName: String,
        numberPlate: String
    ): Task<Void> {

        val orderRef = omc.document(user.config?.omcId!!)
            .collection("orders")
            .document(orderId)

        return firestore.runTransaction { transaction ->

            transaction.update(orderRef, "truck.driverdetail.id", driverId)
            transaction.update(orderRef, "truck.driverdetail.name", driverName)
            transaction.update(orderRef, "truck.truckdetail.numberplate", numberPlate)

            /*
            * update compartment array
            * */
            transaction.update(orderRef, "truck.compartments", compartmentList)

            return@runTransaction null
        }
    }


    fun updatePrintedStateLoading(user: UserModel, orderId: String): CompletionLiveData {
        val completion = CompletionLiveData()
        updatePrintedStateLoadingTask(user, orderId).addOnCompleteListener(completion)

        return completion
    }

    private fun updatePrintedStateLoadingTask(user: UserModel, orderId: String):
            Task<Void> {

        val orderRef = omc.document(user.config?.omcId!!)
            .collection("orders")
            .document(orderId)

        return firestore.runTransaction { transaction ->

            firebaseAuth.currentUser?.let {
                val assocUser =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val p = Printing(true, assocUser)

                transaction.update(orderRef, "printStatus.LoadingOrder", p)
            }

            return@runTransaction null
        }
    }


    fun updatePrintedStateGatePass(user: UserModel, orderId: String): CompletionLiveData {
        val completion = CompletionLiveData()
        updatePrintedStateGatePassTask(user, orderId).addOnCompleteListener(completion)

        return completion
    }

    private fun updatePrintedStateGatePassTask(user: UserModel, orderId: String):
            Task<Void> {

        val orderRef = omc.document(user.config?.omcId!!)
            .collection("orders")
            .document(orderId)

        return firestore.runTransaction { transaction ->

            firebaseAuth.currentUser?.let {
                val assocuser =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val p = Printing(true, assocuser)

                transaction.update(orderRef, "printStatus.gatepass", p)
            }

            return@runTransaction null
        }
    }

    fun moveToQueuing(user: UserModel, orderId: String, minutes: Long):
            CompletionLiveData {
        val completion = CompletionLiveData()
        moveToQueuingTask(user, orderId, minutes).addOnCompleteListener(completion)

        return completion
    }

    private fun moveToQueuingTask(user: UserModel, orderId: String, minutes: Long)
            : Task<Void> {

        val orderRef = omc.document(user.config?.omcId!!)
            .collection("orders")
            .document(orderId)

        return firestore.runTransaction { transaction ->

            val order: OrderModel? = transaction
                .get(orderRef)
                .toObject(OrderModel::class.java)


            // add new time
            val startDate = Calendar.getInstance().time

            val calendar = Calendar.getInstance()
            calendar.time = startDate
            calendar.add(Calendar.MINUTE, minutes.toInt())

            val expireDate = calendar.time
            /**
             *
             * set expirely and the user
             * */
            firebaseAuth.currentUser?.let {
                val setbyExpire =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val expireObj = Expiry(startDate, expireDate, setbyExpire)

                val exp: ArrayList<Expiry>? = order?.truckStageData!!["1"]?.expiry
                exp?.add(0, expireObj)

                // commit to fireStore
                transaction.update(orderRef, "truckStageData.2.expiry", exp)

                // change stage to queueing
                transaction.update(orderRef, "truck.stage", 2)
            }

            return@runTransaction null

        }
    }


    /*
    * update processing expire time
    * */
    fun updateQueueExpire(
        user: UserModel,
        order: OrderModel,
        minutes: Long
    ): CompletionLiveData {
        val completion = CompletionLiveData()
        updateQueueExpireTask(user, order, minutes).addOnCompleteListener(completion)
        return completion
    }


    private fun updateQueueExpireTask(
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
             *
             * set expirely and the user
             * */
            firebaseAuth.currentUser?.let {
                val setbyExpire =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val expireObj = Expiry(startDate, expireDate, setbyExpire)

                val exp: ArrayList<Expiry>? = order?.truckStageData!!["2"]?.expiry
                exp?.add(0, expireObj)

                // commit to fireStore
                transaction.update(orderRef, "truckStageData.2.expiry", exp)
            }

            return@runTransaction null
        }

    }


    fun pushToLoading(user: UserModel, orderId: String, minutes: Long):
            CompletionLiveData {
        val completion = CompletionLiveData()
        pushToLoadingTask(user, orderId, minutes).addOnCompleteListener(completion)

        return completion
    }

    private fun pushToLoadingTask(user: UserModel, orderId: String, minutes: Long)
            : Task<Void> {

        val orderRef = omc.document(user.config?.omcId!!)
            .collection("orders")
            .document(orderId)

        return firestore.runTransaction { transaction ->

            val order: OrderModel? = transaction
                .get(orderRef)
                .toObject(OrderModel::class.java)


            // add new time
            val startDate = Calendar.getInstance().time

            val calendar = Calendar.getInstance()
            calendar.time = startDate
            calendar.add(Calendar.MINUTE, minutes.toInt())

            val expireDate = calendar.time
            /**
             *
             * set expirely and the user
             * */
            firebaseAuth.currentUser?.let {
                val setbyExpire =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val expireObj = Expiry(startDate, expireDate, setbyExpire)

                val exp: ArrayList<Expiry>? = order?.truckStageData!!["3"]?.expiry
                exp?.add(0, expireObj)

                // commit to fireStore
                transaction.update(orderRef, "truckStageData.3.expiry", exp)

                // change stage to queueing
                transaction.update(orderRef, "truck.stage", 3)
            }

            return@runTransaction null

        }
    }
}