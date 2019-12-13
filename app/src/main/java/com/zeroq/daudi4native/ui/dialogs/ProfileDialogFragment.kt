package com.zeroq.daudi4native.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseUser
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.data.models.DepotModel
import kotlinx.android.synthetic.main.fragment_profile_dialog.*

class ProfileDialogFragment(var user: FirebaseUser, var depo: DepotModel) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_profile_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initView() {


        Glide.with(activity!!.applicationContext)
            .load(user.photoUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_circle)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_profile)


        tv_name.text = user.displayName
        tv_email.text = user.email

        tv_depo.text = depo.Name

        close_dialog.setOnClickListener {
            dismiss()
        }
    }


}