package com.zeroq.daudi4native.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.vo.Resource
import javax.inject.Inject

class SplashViewModel @Inject constructor(var firebaseAuth: FirebaseAuth, adminRepo: AdminRepository) :
    ViewModel() {

    private val _userId = MutableLiveData<String>()
    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()

    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)
    }

    private var _isSignedIn: LiveData<Boolean> = object : LiveData<Boolean>() {
        override fun onActive() {
            super.onActive()
            val fireUser: FirebaseUser? = firebaseAuth.currentUser

            value = fireUser != null

            if (fireUser != null) {
                _userId.value = fireUser.uid
            }
        }
    }


    fun isSignedIn(): LiveData<Boolean> {
        return _isSignedIn
    }

    fun getAdmin(): LiveData<Resource<UserModel>> {
        return _user
    }

}