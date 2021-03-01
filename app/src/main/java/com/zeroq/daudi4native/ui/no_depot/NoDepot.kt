package com.zeroq.daudi4native.ui.no_depot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.databinding.ActivityNoDepotBinding
import com.zeroq.daudi4native.ui.MainActivity
import javax.inject.Inject


class NoDepot : BaseActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var authUI: AuthUI

    private lateinit var viewModel: NoDepotViewModel
    private lateinit var binding: ActivityNoDepotBinding

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, NoDepot::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoDepotBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = getViewModel(NoDepotViewModel::class.java)

        firebaseAuth.currentUser?.let {
            binding.displayNameTextView?.text = it.displayName

            Glide.with(this)
                .load(it.photoUrl)
                .centerCrop()
                .placeholder(R.drawable.place_holder)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.adminImageView)
        }

        // logout
        binding.logoutBtnExt.setOnClickListener { authUI.signOut(this) }

        viewModel.getUser().observe(this, Observer {
            if (it.isSuccessful) {
                it.data()?.config?.app?.depotid?.let { depotId ->
                    if (depotId.isNotEmpty()) {
                        MainActivity.startActivity(this)
                        finish()
                    }
                }

            }
        })
    }
}
