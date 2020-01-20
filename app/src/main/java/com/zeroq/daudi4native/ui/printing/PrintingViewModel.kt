package com.zeroq.daudi4native.ui.printing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.data.repository.DepotRepository
import com.zeroq.daudi4native.data.repository.OmcRepository
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.Resource
import javax.inject.Inject

class PrintingViewModel @Inject constructor(
    var adminRepo: AdminRepository,
    var omcRepository: OmcRepository,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()
    private val _userId = MutableLiveData<String>()

    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)

        // init fetching of admin data
        _userId.value = firebaseAuth.uid
    }

    fun getUser(): LiveData<Resource<UserModel>> {
        return _user
    }

    fun setLoadingPrintedState(user: UserModel, orderId: String): CompletionLiveData {
        return omcRepository.updatePrintedStateLoading(user, orderId)
    }

    fun setGatePassPrintedState(user: UserModel, orderId: String): CompletionLiveData {
        return omcRepository.updatePrintedStateGatePass(user, orderId)
    }
}