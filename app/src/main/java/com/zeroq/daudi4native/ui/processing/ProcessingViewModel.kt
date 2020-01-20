package com.zeroq.daudi4native.ui.processing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.data.repository.DepotRepository
import com.zeroq.daudi4native.vo.Resource
import javax.inject.Inject
import com.zeroq.daudi4native.data.models.TruckModel
import com.zeroq.daudi4native.data.repository.OmcRepository
import com.zeroq.daudi4native.vo.CompletionLiveData


class ProcessingViewModel @Inject constructor(
    adminRepo: AdminRepository,
    var depotRepository: DepotRepository,
    var firebaseAuth: FirebaseAuth,
    var omcRepository: OmcRepository
) : ViewModel() {

    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()
    private val _userId = MutableLiveData<String>()

    private val _depotId = MutableLiveData<String>()


    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)

        _userId.value = firebaseAuth.uid
    }

    fun getUser(): LiveData<Resource<UserModel>> {
        return _user
    }

    fun setDepoId(depotid: String) {
        if (depotid != _depotId.value) _depotId.value = depotid
    }

    fun updateExpire(user: UserModel, order: OrderModel, minutes: Long): CompletionLiveData {
        return omcRepository.updateProcessingExpire(user, order, minutes)
    }

    fun moveToQueuing(
        idTruck: String, minutes: Long
    ): CompletionLiveData {
        return depotRepository.pushToueueing(
            _depotId.value!!,
            idTruck,
            minutes,
            firebaseAuth.currentUser!!
        )
    }


    fun moveToQueuing(
        user: UserModel,
        orderId: String,
        minutes: Long
    ): CompletionLiveData {
        return omcRepository.moveToQueuing(user, orderId, minutes)
    }


}