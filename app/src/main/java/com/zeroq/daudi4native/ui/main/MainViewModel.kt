package com.zeroq.daudi4native.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.data.models.DepotModel
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.data.models.TruckModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.data.repository.DepotRepository
import com.zeroq.daudi4native.data.repository.OmcRepository
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.Resource
import javax.inject.Inject

class MainViewModel @Inject constructor(
    var adminRepo: AdminRepository,
    var omcRepo: OmcRepository,
    depotRepository: DepotRepository,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()

    /*
    * orders that are in the required stage
    * */
    private var _orders: LiveData<Resource<List<OrderModel>>> = MutableLiveData()
    private val _userId = MutableLiveData<String>()

    // To trigger components that require user params
    private var _switchUser = MutableLiveData<UserModel>()

    private var _depo: LiveData<Resource<DepotModel>> = MutableLiveData()


    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)
        _depo = Transformations.switchMap(_switchUser, depotRepository::getDepot)


        _orders = Transformations.switchMap(_switchUser, omcRepo::getOrders)


        // init fetching of admin data
        _userId.value = firebaseAuth.uid
    }

    fun setSwitchUser(user: UserModel) {
        if (_switchUser.value != user) {
            _switchUser.value = user
        }
    }

    fun getUser(): LiveData<Resource<UserModel>> {
        return _user
    }


    fun getOrders(): LiveData<Resource<List<OrderModel>>> {
        return _orders
    }

    fun getDepot(): LiveData<Resource<DepotModel>> {
        return _depo
    }

    fun postToken(token: String): CompletionLiveData {
        return adminRepo.postFcmToken(_userId.value!!, token)
    }
}