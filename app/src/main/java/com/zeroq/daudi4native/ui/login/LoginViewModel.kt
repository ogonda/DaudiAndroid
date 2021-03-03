package com.zeroq.daudi4native.ui.login

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.data.repository.AdminRepository
import com.zeroq.daudi4native.vo.MResource
import com.zeroq.daudi4native.vo.Resource
import com.zeroq.daudi4native.vo.Status
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel @Inject constructor(var firebaseAuth: FirebaseAuth, adminRepo: AdminRepository) :
    ViewModel() {

    val RC_SIGN_IN = 200
    private var _loginData = MutableLiveData<MResource<AuthResult>>()
    private var _userId = MutableLiveData<String>()
    private var _user: LiveData<Resource<UserModel>> = MutableLiveData()

    init {
        _user = Transformations.switchMap(_userId, adminRepo::getAdmin)
    }

    //Called from Activity receving result
    fun onResultFromActivity(requestCode: Int, resultCode: Int, data: Intent?) {
        _loginData.value = MResource(Status.LOADING, null, "")

        when (RC_SIGN_IN) {
            requestCode -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account!!)
                } catch (e: ApiException) {
                    Timber.e(e)
                    _loginData.value = MResource(Status.ERROR, null, e.message)
                }
            }
        }
    }

    fun getLogin(): MutableLiveData<MResource<AuthResult>> {
        return _loginData
    }

    fun getUser(): LiveData<Resource<UserModel>> {
        return _user
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener() { task ->
                if (!task.isSuccessful) {
                    _loginData.value = MResource(Status.ERROR, null, task.exception?.message)
                    Timber.e(task.exception)
                }
                _loginData.value = MResource(Status.SUCCESS, task.result, "")
                _userId.value = firebaseAuth.uid

            }
    }
}