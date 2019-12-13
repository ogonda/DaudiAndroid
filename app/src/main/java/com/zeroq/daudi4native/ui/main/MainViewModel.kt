package com.zeroq.daudi4native.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.data.models.DepotModel
import com.zeroq.daudi4native.data.models.TruckModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.data.repository.DepotRepository
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.Resource
import javax.inject.Inject

class MainViewModel @Inject constructor(
    var adminRepo: AdminRepository,
    depotRepository: DepotRepository,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()
    private var _trucks: LiveData<Resource<List<TruckModel>>> = MutableLiveData()
    private val _userId = MutableLiveData<String>()
    private var _depotId = MutableLiveData<String>()
    private var _depo: LiveData<Resource<DepotModel>> = MutableLiveData()

    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)
        _trucks = Transformations.switchMap(_depotId, depotRepository::getAllTrucks)
        _depo = Transformations.switchMap(_depotId, depotRepository::getDepot)


        // init fetching of admin data
        _userId.value = firebaseAuth.uid
    }

    fun setDeportId(depotId: String?) {
        if (_depotId.value != depotId) {
            _depotId.value = depotId
        }
    }

    fun getUser(): LiveData<Resource<UserModel>> {
        return _user
    }

    fun getTrucks(): LiveData<Resource<List<TruckModel>>> {
        return _trucks
    }

    fun getDepot(): LiveData<Resource<DepotModel>> {
        return _depo
    }

    fun postToken(token: String): CompletionLiveData{
        return adminRepo.postFcmToken(_userId.value!!, token)
    }
}