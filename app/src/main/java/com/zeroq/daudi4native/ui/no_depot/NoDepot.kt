package com.zeroq.daudi4native.ui.no_depot

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.commons.BaseActivity
import kotlinx.android.synthetic.main.activity_no_depot.*
import javax.inject.Inject

class NoDepot : BaseActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var authUI: AuthUI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_depot)

        firebaseAuth.currentUser?.let {
            displayNameTextView?.text = it.displayName

            Glide.with(this)
                .load(it.photoUrl)
                .centerCrop()
                .placeholder(R.drawable.place_holder)
                .apply(RequestOptions.circleCropTransform())
                .into(adminImageView)
        }

        // logout
        logout_btn_ext.setOnClickListener { authUI.signOut(this) }

    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, NoDepot::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }
}
