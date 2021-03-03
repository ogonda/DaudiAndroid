package com.zeroq.daudi4native.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.data.models.TruckModel
import com.zeroq.daudi4native.databinding.FragmentTimeDialogBinding
import com.zeroq.daudi4native.ui.dialogs.data.TimeDialogEvent
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class TimeDialogFragment(var title: String, var order: OrderModel) : DialogFragment() {

    private var _binding: FragmentTimeDialogBinding? = null

    private val binding get() = _binding

    private var _minutes: Int = 0
    private var _hours: Int = 0

    var timeEvent = PublishSubject.create<TimeDialogEvent>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentTimeDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewInit()
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun viewInit() {

        binding?.tvDialogTitle?.text = title
        binding?.tvTruckId?.text = "Truck ${order.QbConfig?.InvoiceId}"

        binding?.ibUpHour?.setOnClickListener {
            _hours += 1
            if (_hours > 5) _hours = 0
            binding?.tvHour?.text = _hours.toString()
        }

        binding?.ibDownHour?.setOnClickListener {
            _hours -= 1
            if (_hours < 0) _hours = 5
            binding?.tvHour?.text = _hours.toString()
        }

        /*
        * minutes
        * */
        binding?.ibUpMinute?.setOnClickListener {
            _minutes += 10
            if (_minutes > 60) _minutes = 0
            binding?.tvMinute?.text = _minutes.toString()
        }

        binding?.ibDownMinute?.setOnClickListener {
            _minutes -= 10
            if (_minutes < 0) _minutes = 60
            binding?.tvMinute?.text = _minutes.toString()
        }

        /**
         * cancel and set buttons
         * */
        binding?.tvCancel?.setOnClickListener {
            dismiss()
        }

        binding?.tvSet?.setOnClickListener {
            if ((_hours + _minutes) == 0) {
                Toast.makeText(activity, "you need to add time", Toast.LENGTH_SHORT).show()
            } else {
                val hourIntoMin = _hours * 60
                timeEvent.onNext(
                    TimeDialogEvent(
                        (hourIntoMin + _minutes),
                        order
                    )
                )
                dismiss()
            }
        }

    }
}