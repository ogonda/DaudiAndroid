package com.zeroq.daudi4native.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.zeroq.daudi4native.data.models.*
import com.zeroq.daudi4native.ui.dialogs.data.LoadingDialogEvent
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.DocumentLiveData
import com.zeroq.daudi4native.vo.QueryLiveData
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList
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
    * update expire time
    * */
    fun updateTruckExpiry(
        user: UserModel,
        order: OrderModel,
        minutes: Long,
        stage: Int
    ): CompletionLiveData {
        val completion = CompletionLiveData()
        updateProcessingExpireTask(user, order, minutes, stage).addOnCompleteListener(completion)

        return completion
    }


    private fun updateProcessingExpireTask(
        user: UserModel,
        orderData: OrderModel,
        minutes: Long,
        stage: Int
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
                val assocUser =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val expireObj = Expiry(startDate, expireDate, assocUser)

                val exp: ArrayList<Expiry>? = order?.truckStageData!!["$stage"]?.expiry
                exp?.add(0, expireObj)

                // commit to fireStore
                transaction.update(orderRef, "truckStageData.$stage.expiry", exp)


                // calculate the other values
                val Additions = exp!!.size

                val totalApproxTime =
                    order.truckStageData!!["$stage"]?.totalApproxTime!! + (expireDate.time - startDate.time)

                /*
                * should be updated on the next stage also.
                * when pushing to the next stage
                *
                * - if array size is bigger than 2,  take start time and expire of the previous expire.
                * */
                val totalExpiredTimeTemp = if (exp.size >= 2) {
                    exp[0].timeCreated!!.time - exp[stage].timeCreated!!.time
                } else {
                    0
                }

                val totalExpiredTime =
                    order.truckStageData!!["$stage"]?.totalExpiredTime!! + totalExpiredTimeTemp


                // commit all these changes
                transaction.update(orderRef, "truckStageData.$stage.Additions", Additions)
                transaction.update(
                    orderRef,
                    "truckStageData.$stage.totalExpiredTime",
                    totalExpiredTime
                )
                transaction.update(
                    orderRef,
                    "truckStageData.$stage.totalApproxTime",
                    totalApproxTime
                )

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

            val order: OrderModel? = transaction
                .get(orderRef)
                .toObject(OrderModel::class.java)


            firebaseAuth.currentUser?.let {
                val assocuser =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val p = Printing(true, assocuser)

                transaction.update(orderRef, "printStatus.gatepass", p)

                transaction.update(orderRef, "truck.stage", 4)
                
                val totalExpiredTimeTemp =
                    if (Calendar.getInstance().time.time > order!!.truckStageData!!["1"]?.expiry!![0].expiry!!.time) {
                        Calendar.getInstance().time.time - order.truckStageData!!["1"]?.expiry!![0].expiry!!.time
                    } else {
                        0
                    }

                val totalExpiredTime =
                    order.truckStageData!!["3"]?.totalExpiredTime!! + totalExpiredTimeTemp

                /*
                * will add time to the total expired time.
                * */
                transaction.update(orderRef, "truckStageData.3.totalExpiredTime", totalExpiredTime)
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
                val assocUser =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val expireObj = Expiry(startDate, expireDate, assocUser)

                val exp: ArrayList<Expiry>? = order?.truckStageData!!["1"]?.expiry
                exp?.add(0, expireObj)

                // commit to fireStore
                transaction.update(orderRef, "truckStageData.2.expiry", exp)

                // user who moved the truck to this stage
                transaction.update(orderRef, "truckStageData.2.user", assocUser)

                // user who moved the the order.
                transaction.update(orderRef, "orderStageData.2.user", assocUser)

                // change stage to queueing
                transaction.update(orderRef, "truck.stage", 2)


                /*
                * calculate the totalExpiredTime
                * */
                val totalExpiredTimeTemp =
                    if (exp!![0].timeCreated!!.time > order.truckStageData!!["1"]?.expiry!![0].expiry!!.time) {
                        exp[0].timeCreated!!.time - exp[1].timeCreated!!.time
                    } else {
                        0
                    }

                val totalExpiredTime =
                    order.truckStageData!!["1"]?.totalExpiredTime!! + totalExpiredTimeTemp

                /*
                * will add time to the total expired time.
                * */
                transaction.update(orderRef, "truckStageData.1.totalExpiredTime", totalExpiredTime)
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
                val associatedUser =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val expireObj = Expiry(startDate, expireDate, associatedUser)

                val exp: ArrayList<Expiry>? = order?.truckStageData!!["3"]?.expiry
                exp?.add(0, expireObj)

                // commit to fireStore
                transaction.update(orderRef, "truckStageData.3.expiry", exp)

                // user who moved the truck to this stage
                transaction.update(orderRef, "truckStageData.3.user", associatedUser)

                // user who moved the the order.
                transaction.update(orderRef, "orderStageData.3.user", associatedUser)

                // change stage to queueing
                transaction.update(orderRef, "truck.stage", 3)


                /*
               * calculate the totalExpiredTime
               * */
                val totalExpiredTimeTemp =
                    if (exp!![0].timeCreated!!.time > order.truckStageData!!["2"]?.expiry!![0].expiry!!.time) {
                        exp[0].timeCreated!!.time - order.truckStageData!!["2"]?.expiry!![0].expiry!!.time
                    } else {
                        0
                    }

                val totalExpiredTime =
                    order.truckStageData!!["2"]?.totalExpiredTime!! + totalExpiredTimeTemp

                /*
                * will add time to the total expired time.
                * */
                transaction.update(orderRef, "truckStageData.2.totalExpiredTime", totalExpiredTime)
            }

            return@runTransaction null

        }
    }

    // update seals, update fuel reserves
    fun updateSealAndFuel(
        user: UserModel,
        loadingEvent: LoadingDialogEvent,
        orderId: String,
        depotModel: DepotModel?
    ): CompletionLiveData {
        val completion = CompletionLiveData()
        updateSealAndFuelTask(user, loadingEvent, orderId, depotModel?.config?.private!!)
            .addOnCompleteListener(completion)
        return completion
    }

    private fun updateSealAndFuelTask(
        userModel: UserModel,
        loadingEvent: LoadingDialogEvent,
        orderId: String,
        isPrivate: Boolean
    ): Task<Void> {
        val orderRef = omc.document(userModel.config?.omcId!!)
            .collection("orders")
            .document(orderId)


        return firestore.runTransaction { transaction ->
            val order: OrderModel? = transaction.get(orderRef).toObject(OrderModel::class.java)

            /**
             * let get other elements
             *
             * 1. update truck.fuel.FUELtTYPE.batches["0|1"].
             * */
            val fuels = listOf(
                Triple("pms", order?.fuel?.pms, loadingEvent.pmsLoaded),
                Triple("ago", order?.fuel?.ago, loadingEvent.agoLoaded),
                Triple("ik", order?.fuel?.ik, loadingEvent.ikLoaded)
            )


            fuels.forEach { triple ->
                val bQuantity = triple.second?.qty

                if (bQuantity != null && bQuantity > 0) {
                    val updated: Batches = mutateFuelObservered(triple.second!!, triple.third)

                    // update fuel
                    transaction.update(
                        orderRef,
                        "fuel.${triple.first}.entries",
                        updated.entries
                    )
                }
            }


            /*
           * update seals with user
           * */
            firebaseAuth.currentUser?.let {
                val assocUser =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val sealsTemp = OrderSeals(
                    ArrayList(loadingEvent.sealRange?.split("-")!!),
                    ArrayList(loadingEvent.brokenSeal?.split("-")!!),
                    assocUser
                )

                transaction.update(orderRef, "seals", sealsTemp)

                /**
                 * update delivery note number
                 * */
                val deliveryNote = DeliveryNote(loadingEvent.DeliveryNumber)

                transaction.update(
                    orderRef,
                    "deliveryNote",
                    deliveryNote
                )
            }

            return@runTransaction null
        }
    }


    private fun mutateFuelObservered(fuel: Batches, observed: Int?): Batches {
        val position = fuel.entries!!.size - 1

        fuel.entries!![position].observed = observed
        return fuel;
    }


    // update seals and delivery note number
    fun updateSealInfo(
        userModel: UserModel,
        orderId: String,
        sealRange: String,
        brokenSeals: String,
        delivery: String
    ): CompletionLiveData {
        val completion = CompletionLiveData()
        updateSealInfoTask(
            userModel,
            orderId,
            sealRange,
            brokenSeals,
            delivery
        ).addOnCompleteListener(completion)

        return completion
    }


    private fun updateSealInfoTask(
        userModel: UserModel, orderId: String,
        sealRange: String,
        brokenSeals: String,
        delivery: String
    ): Task<Void> {
        val orderRef = omc.document(userModel.config?.omcId!!)
            .collection("orders")
            .document(orderId)

        return firestore.runTransaction { transaction ->
            firebaseAuth.currentUser?.let {
                val assocUser =
                    AssociatedUser(it.displayName, it.uid, Calendar.getInstance().time)

                val sealsTemp = OrderSeals(
                    ArrayList(sealRange.split("-")),
                    ArrayList(brokenSeals.split("-")),
                    assocUser
                )

                transaction.update(orderRef, "seals", sealsTemp)

                /**
                 * update delivery note number
                 * */
                val deliveryNote = DeliveryNote(delivery)

                transaction.update(
                    orderRef,
                    "deliveryNote",
                    deliveryNote
                )
            }

            return@runTransaction null
        }
    }
}