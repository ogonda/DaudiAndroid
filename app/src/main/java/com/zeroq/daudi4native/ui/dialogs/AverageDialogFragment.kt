package com.zeroq.daudi4native.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.adapters.OmcSpinnerAdapter
import com.zeroq.daudi4native.data.models.OmcModel
import com.zeroq.daudi4native.ui.dialogs.data.AverageDialogEvent
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_average_dialog.*
import org.jetbrains.anko.toast

class AverageDialogFragment(var omcs: List<OmcModel>) : DialogFragment() {

    var averageEvent =
        PublishSubject.create<AverageDialogEvent>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(
            R.layout.fragment_average_dialog,
            container, false
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

        val adapter = OmcSpinnerAdapter(activity!!.baseContext, R.layout.spinner_row, ArrayList(omcs))
        spinner.adapter = adapter

        fuelCancel.setOnClickListener {
            dismiss()
        }

        fuelSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }


    private fun validateAndSubmit() {
        if (!pmsPrice.text.isBlank() || !agoPrice.text.isBlank() || !ikPrice.text.isBlank()) {
            val spinnerOmc = spinner.selectedItem as OmcModel

            val pmcValue = if (pmsPrice.text.isBlank()) null else pmsPrice.text.toString().toDouble()
            val agoValue = if (agoPrice.text.isBlank()) null else agoPrice.text.toString().toDouble()
            val ikValue = if (ikPrice.text.isBlank()) null else ikPrice.text.toString().toDouble()


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