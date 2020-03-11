package com.zeroq.daudi4native.ui.loading_order

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.zeroq.daudi4native.data.models.DepotModel
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.data.models.TruckModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.data.repository.DepotRepository
import com.zeroq.daudi4native.data.repository.OmcRepository
import com.zeroq.daudi4native.data.repository.UploadRepository
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.Resource
import com.zeroq.daudi4native.vo.combineLatest
import javax.inject.Inject

class LoadingOrderViewModel @Inject constructor(
    adminRepo: AdminRepository,
    var depotRepository: DepotRepository,
    var firebaseAuth: FirebaseAuth,
    var omcRepository: OmcRepository,
    var uploadRepository: UploadRepository
) : ViewModel() {

    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()

    private val _userId = MutableLiveData<String>()
    private var _depotId = MutableLiveData<String>()


    // To trigger components that require user params
    private var _switchUser = MutableLiveData<UserModel>()
    private var _depo: LiveData<Resource<DepotModel>> = MutableLiveData()
    private var _order: LiveData<Resource<OrderModel>> = MutableLiveData()
    private var _combinedUserOrderId = MutableLiveData<Pair<UserModel, String>>()

    private val _orderId = MutableLiveData<String>()


    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)
        _depo = Transformations.switchMap(_switchUser, depotRepository::getDepot)
        _order = Transformations.switchMap(_combinedUserOrderId, omcRepository::getOrder)

        _switchUser.combineLatest(_orderId).observeForever {
            _combinedUserOrderId.value = it
        }

        _userId.value = firebaseAuth.uid
    }

    fun setSwitchUser(user: UserModel) {
        if (_switchUser.value != user) {
            _switchUser.value = user
        }
    }

    fun setDepotId(depotId: String) {
        if (depotId != _depotId.value) _depotId.value = depotId
    }

    fun setOrderId(orderId: String) {
        if (orderId != _orderId.value) _orderId.value = orderId
    }

    fun getUser(): LiveData<Resource<UserModel>> {
        return _user
    }

    fun getDepot(): LiveData<Resource<DepotModel>> {
        return _depo
    }

    fun getOrder(): LiveData<Resource<OrderModel>> {
        return _order
    }

    fun updateSeals(
        user: UserModel, orderId: String, sealRange: String, brokenSeals: String, delivery: String
    ): CompletionLiveData {
        return omcRepository.updateSealInfo(
            user, orderId, sealRange, brokenSeals, delivery
        )
    }

    fun uploadNote(bitmap: Bitmap, order: OrderModel): Pair<UploadTask, StorageReference> {
        return uploadRepository.uploadNote(bitmap, order);
    }

    fun addDeliveryNotePath(
        userModel: UserModel,
        orderId: String,
        path: String
    ): CompletionLiveData {
        return omcRepository.addDeliveryNotePath(userModel, orderId, path)
    }

}