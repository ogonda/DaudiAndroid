package com.zeroq.daudi4native.ui.average_prices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.data.models.AveragePriceModel
import com.zeroq.daudi4native.data.models.OmcModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.data.repository.DepotRepository
import com.zeroq.daudi4native.data.repository.OmcRepository
import com.zeroq.daudi4native.ui.dialogs.data.AverageDialogEvent
import com.zeroq.daudi4native.vo.CompletionLiveData
import com.zeroq.daudi4native.vo.Resource
import javax.inject.Inject

class AverageViewModel @Inject constructor(
    var omcRepository: OmcRepository,
    var firebaseAuth: FirebaseAuth,
    var depotRepository: DepotRepository,
    var adminRepo: AdminRepository
) : ViewModel() {

    private var omcsLive: LiveData<Resource<List<OmcModel>>> = MutableLiveData()
    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()

    private var todaysFuelPrices: LiveData<Resource<List<AveragePriceModel>>> = MutableLiveData()

    private var _depotId = MutableLiveData<String>()
    private val _userId = MutableLiveData<String>()

    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)
        omcsLive = omcRepository.getAllOmcs()

        todaysFuelPrices = Transformations.switchMap(_depotId, depotRepository::getTodaysFuelPrices)

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

    fun getOmcs(): LiveData<Resource<List<OmcModel>>> {
        return omcsLive
    }


    fun postOmcAveragePrice(data: AverageDialogEvent): CompletionLiveData {
        return depotRepository.addFuelPriceFromOmcs(
            _depotId.value!!,
            firebaseAuth.currentUser!!,
            data
        )
    }


    fun getTodayPrices(): LiveData<Resource<List<AveragePriceModel>>> {
        return todaysFuelPrices
    }

    fun deletePrice(fuelPriceId: String): CompletionLiveData {
        return depotRepository.deleteFuelPrices(_depotId.value!!, fuelPriceId)
    }
}