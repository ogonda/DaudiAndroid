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
import com.zeroq.daudi4native.databinding.ActivityActivateBinding
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class ActivateActivity : DaggerAppCompatActivity() {

    private lateinit var binding: ActivityActivateBinding

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
        binding = ActivityActivateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initialize()
    }

    private fun initialize() {
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        Glide.with(this)
            .load(firebaseUser?.photoUrl)
            .centerCrop()
            .placeholder(R.drawable.place_holder)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.adminImageView)

        binding.displayNameTextView.text = firebaseUser?.displayName

        // logout
        binding.logoutBtn.setOnClickListener { authUI.signOut(this) }
    }

}
