package com.zeroq.daudi4native.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.data.models.TruckModel
import com.zeroq.daudi4native.ui.dialogs.data.TimeDialogEvent
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_time_dialog.*

class TimeDialogFragment(var title: String, var truck: TruckModel) : DialogFragment() {

    private var _minutes: Int = 0
    private var _hours: Int = 0

    var timeEvent = PublishSubject.create<TimeDialogEvent>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(
            R.layout.fragment_time_dialog,
            container,
            false
        )
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

        tv_dialog_title.text = title
        tv_truck_id.text = "Truck ${truck.truckId}"


        ib_up_hour.setOnClickListener {
            _hours += 1
            if (_hours > 5) _hours = 0
            tv_hour.text = _hours.toString()
        }

        ib_down_hour.setOnClickListener {
            _hours -= 1
            if (_hours < 0) _hours = 5
            tv_hour.text = _hours.toString()
        }

        /*
        * minutes
        * */
        ib_up_minute.setOnClickListener {
            _minutes += 10
            if (_minutes > 60) _minutes = 0
            tv_minute.text = _minutes.toString()
        }

        ib_down_minute.setOnClickListener {
            _minutes -= 10
            if (_minutes < 0) _minutes = 60
            tv_minute.text = _minutes.toString()
        }

        /**
         * cancel and set buttons
         * */
        tv_cancel.setOnClickListener {
            dismiss()
        }

        tv_set.setOnClickListener {
            if ((_hours + _minutes) == 0) {
                Toast.makeText(activity, "you need to add time", Toast.LENGTH_SHORT).show()
            } else {
                val hourIntoMin = _hours * 60
                timeEvent.onNext(
                    TimeDialogEvent(
                        (hourIntoMin + _minutes),
                        truck
                    )
                )
                dismiss()
            }
        }

    }
}