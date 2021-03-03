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
import com.zeroq.daudi4native.databinding.FragmentProfileDialogBinding
import timber.log.Timber
import java.lang.Exception

class ProfileDialogFragment(var user: FirebaseUser, var depo: DepotModel) : DialogFragment() {

    private var _binding: FragmentProfileDialogBinding? = null

    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileDialogBinding.inflate(inflater, container, false)
        return binding?.root
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


        binding?.let {
            Glide.with(requireActivity().applicationContext)
                .load(user.photoUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_circle)
                .apply(RequestOptions.circleCropTransform())
                .into(it.ivProfile)
        }


        binding?.tvName?.text = user.displayName
        binding?.tvEmail?.text = user.email

        binding?.tvDepo?.text = depo.Name

        binding?.closeDialog?.setOnClickListener {
            dismiss()
        }
    }


}