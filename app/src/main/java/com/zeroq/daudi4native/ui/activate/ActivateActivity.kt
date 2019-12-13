package com.zeroq.daudi4native.ui.activate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.zeroq.daudi4native.R
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_activate.*
import javax.inject.Inject

class ActivateActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var authUI: AuthUI

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, ActivateActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activate)
        initialize()
    }

    private fun initialize() {
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        Glide.with(this)
            .load(firebaseUser?.photoUrl)
            .centerCrop()
            .placeholder(R.drawable.place_holder)
            .apply(RequestOptions.circleCropTransform())
            .into(adminImageView)

        displayNameTextView.text = firebaseUser?.displayName

        // logout
        logout_btn.setOnClickListener { authUI.signOut(this) }
    }

}
