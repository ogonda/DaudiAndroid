package com.zeroq.daudi4native.ui.truck_detail

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.data.models.*
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.data.repository.DepotRepository
import com.zeroq.daudi4native.data.repository.OmcRepository
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.Resource
import com.zeroq.daudi4native.vo.combineLatest
import javax.inject.Inject

class TruckDetailViewModel @Inject constructor(
    adminRepo: AdminRepository,
    var depotRepository: DepotRepository,
    var omcRepository: OmcRepository,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()
    private var _truck: LiveData<Resource<TruckModel>> = MutableLiveData()

    private var _order: LiveData<Resource<OrderModel>> = MutableLiveData()

    private val _userId = MutableLiveData<String>()
    private val _truckId = MutableLiveData<String>()
    private var _depotId = MutableLiveData<String>()

    // get data
    private val _userModel = MutableLiveData<UserModel>()
    private val _orderId = MutableLiveData<String>()

    private var _combinedDepoTruckId = MutableLiveData<Pair<String, String>>()
    private var _combinedUserOrderId = MutableLiveData<Pair<UserModel, String>>()
    private var _depo: LiveData<Resource<DepotModel>> = MutableLiveData()


    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)

        _depotId.combineLatest(_truckId).observeForever(Observer {
            _combinedDepoTruckId.value = it
        })

        _userModel.combineLatest(_orderId).observeForever {
            _combinedUserOrderId.value = it
        }

        _truck = Transformations.switchMap(_combinedDepoTruckId, depotRepository::getTruck)

        _order = Transformations.switchMap(_combinedUserOrderId, omcRepository::getOrder)

        _userId.value = firebaseAuth.uid
    }


    fun setUserModel(userModel: UserModel) {
        if (userModel != _userModel.value) _userModel.value = userModel
    }


    fun setOrderId(orderId: String) {
        if (orderId != _orderId.value) _orderId.value = orderId
    }

    fun setDepotId(depotId: String) {
        if (depotId != _depotId.value) _depotId.value = depotId
    }

    fun getUser(): LiveData<Resource<UserModel>> {
        return _user
    }

    fun getTruck(): LiveData<Resource<TruckModel>> {
        return _truck
    }

    fun getOrder(): LiveData<Resource<OrderModel>> {
        return _order
    }

    fun getDepot(): LiveData<Resource<DepotModel>> {
        return _depo
    }

    fun updateTruckComAndDriver(
        depotId: String,
        idTruck: String,
        compartmentList: List<Compartment>,
        driverId: String, driverName: String, numberPlate: String
    ): CompletionLiveData {
        return depotRepository.updateCompartmentAndDriver(
            depotId, idTruck, compartmentList,
            driverId, driverName, numberPlate
        )
    }

}