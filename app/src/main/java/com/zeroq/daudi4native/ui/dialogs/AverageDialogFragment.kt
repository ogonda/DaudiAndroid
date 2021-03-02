package com.zeroq.daudi4native.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.adapters.OmcSpinnerAdapter
import com.zeroq.daudi4native.data.models.OmcModel
import com.zeroq.daudi4native.databinding.FragmentAverageDialogBinding
import com.zeroq.daudi4native.ui.dialogs.data.AverageDialogEvent
import io.reactivex.subjects.PublishSubject
import org.jetbrains.anko.toast
import timber.log.Timber

class AverageDialogFragment(var omcs: List<OmcModel>) : DialogFragment() {

    private var _binding: FragmentAverageDialogBinding? = null

    private val binding get() = _binding

    var averageEvent =
        PublishSubject.create<AverageDialogEvent>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        try
        {
        _binding = FragmentAverageDialogBinding.inflate(inflater, container, false)
        }
    catch (e: Exception){
        Timber.e(e)
    }
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

        val adapter = OmcSpinnerAdapter(requireActivity().baseContext, R.layout.spinner_row, ArrayList(omcs))
        binding?.spinner!!.adapter = adapter

        binding?.fuelCancel!!.setOnClickListener {
            dismiss()
        }

        binding?.fuelSubmit!!.setOnClickListener {
            validateAndSubmit()
        }
    }


    private fun validateAndSubmit() {
        if (!binding?.pmsPrice!!.text.isBlank() || !binding?.agoPrice!!.text.isBlank() || !binding?.ikPrice!!.text.isBlank()) {
            val spinnerOmc = binding?.spinner!!.selectedItem as OmcModel

            val pmcValue = if (binding?.pmsPrice!!.text.isBlank()) null else binding?.pmsPrice!!.text.toString().toDouble()
            val agoValue = if (binding?.agoPrice!!.text.isBlank()) null else binding?.agoPrice!!.text.toString().toDouble()
            val ikValue = if (binding?.ikPrice!!.text.isBlank()) null else binding?.ikPrice!!.text.toString().toDouble()


            val avgEvent = AverageDialogEvent(
                pmcValue, agoValue,
                ikValue, spinnerOmc
            )

            averageEvent.onNext(avgEvent)
            dismiss()

        } else {
            activity?.toast("Please fill at least one fuel")
        }
    }
}