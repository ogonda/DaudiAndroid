package com.zeroq.daudi4native.di.modules

import android.app.Application
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.R
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AuthModule {

    @Provides
    @Singleton
    fun getGoogleClient(app: Application): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(app.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(app, gso)
    }

    @Provides
    @Singleton
    fun getFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun getAuthUi(): AuthUI {
        return AuthUI.getInstance()
    }
}