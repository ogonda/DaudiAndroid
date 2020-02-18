package com.zeroq.daudi4native.ui.loading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.data.models.DepotModel
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.data.repository.DepotRepository
import com.zeroq.daudi4native.data.repository.OmcRepository
import com.zeroq.daudi4native.ui.dialogs.data.LoadingDialogEvent
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.Resource
import javax.inject.Inject

class LoadingViewModel @Inject constructor(
    adminRepo: AdminRepository,
    var depotRepository: DepotRepository,
    var firebaseAuth: FirebaseAuth,
    var omcRepository: OmcRepository
) : ViewModel() {

    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()
    private val _userId = MutableLiveData<String>()

    private val _depotId = MutableLiveData<String>()

    private var _depo: LiveData<Resource<DepotModel>> = MutableLiveData()

    // To trigger components that require user params
    private var _switchUser = MutableLiveData<UserModel>()

    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)
        _depo = Transformations.switchMap(_switchUser, depotRepository::getDepot)

        _userId.value = firebaseAuth.uid
    }

    fun getUser(): LiveData<Resource<UserModel>> {
        return _user
    }

    fun getDepot(): LiveData<Resource<DepotModel>> {
        return _depo
    }

    fun setSwitchUser(user: UserModel) {
        if (_switchUser.value != user) {
            _switchUser.value = user
        }
    }

    fun setDepoId(depotid: String) {
        if (depotid != _depotId.value) _depotId.value = depotid
    }

    fun updateExpire(user: UserModel, order: OrderModel, minutes: Long): CompletionLiveData {
        return omcRepository.updateTruckExpiry(user, order, minutes, 3)
    }

    fun updateSeals(
        user: UserModel,
        loadingEvent: LoadingDialogEvent,
        orderId: String,
        depot: DepotModel
    ): CompletionLiveData {
        return omcRepository.updateSealAndFuel(user, loadingEvent, orderId, depot)
    }
}