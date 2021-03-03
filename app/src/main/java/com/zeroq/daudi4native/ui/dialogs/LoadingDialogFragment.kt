package com.zeroq.daudi4native.ui.dialogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.databinding.FragmentLoadingDialogBinding
import com.zeroq.daudi4native.ui.dialogs.data.LoadingDialogEvent
import io.reactivex.subjects.PublishSubject
import kotlin.math.abs
import timber.log.Timber

class LoadingDialogFragment(var order: OrderModel) : DialogFragment() {

    private var _binding: FragmentLoadingDialogBinding? = null

    private val binding get() = _binding

    var loadingEvent =
        PublishSubject.create<LoadingDialogEvent>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoadingDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }
    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewInit()
    }


    lateinit var actuals: List<Pair<EditText, Int>>
    lateinit var belowViews: List<EditText>

    private fun viewInit() {
        binding?.tvLoadingTruckId?.text = "Truck ${order.QbConfig?.InvoiceId}"

        /**
         * cancel dialog
         * */
        binding?.tvLoadingCancel?.setOnClickListener {
            dismiss()
        }

        actuals = listOf(
            Pair(binding?.etPmsActual!!, order.fuel?.pms?.qty!!),
            Pair(binding?.etAgoActual!!, order.fuel?.ago?.qty!!), Pair(binding?.etIkActual!!, order.fuel?.ik?.qty!!)
        )

        /**
         * may improve this code later
         * */

        actuals.forEach {
            hideView(it.first, it.second)

            val allowedActual = 0.9




            if (it.second > 0) {
                it.first.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {


                        if (!s.isNullOrEmpty()) {
                            val minActual = abs(allowedActual * it.second).toInt()
                            val providedActual = s.toString().toInt()

                            if ((minActual < providedActual) && (providedActual < it.second)) {
                                it.first.error = null
                            }
                        } else {
                            if (s.isNullOrEmpty()) it.first.error = "This field can't be empty"
                        }
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (!s.isNullOrEmpty()) {
                            val minActual = abs(allowedActual * it.second).toInt()
                            val providedActual = s.toString().toInt()

                            if (providedActual < minActual) {
                                it.first.error =
                                    "Amount should be between $minActual and ${it.second}"
                            }

                            if (providedActual > it.second) {
                                it.first.error = "Amount cant be more than ${it.second}"
                            }
                        }
                    }
                })
            }

            /**
             * make sure seals, broken, and delivery note number are not invalid
             * */
            belowViews = listOf(binding?.etSealRange!!, binding?.etBrokenSeal!!, binding?.etDeliveryNumber!!)

            belowViews.forEach { editText ->
                editText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        if (!s.isNullOrEmpty()) editText.error = null

                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        if (s.isNullOrEmpty()) {
                            editText.error = "This field can't be empty"
                        }
                    }
                })
            }


            /**
             * check if everything is valid
             * */
            binding?.tvLoadingSubmit?.setOnClickListener { validateForm() }
        }


    }

    private fun hideView(view: EditText, quantity: Int) {
        if (quantity <= 0) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    private fun validateForm() {
        /**
         * check if fuel is okay
         * */
        var hasErrors = false

        actuals.forEach { pair ->
            if (pair.second > 0) {
                if (pair.first.text.isNullOrEmpty() || pair.first.error != null) {
                    hasErrors = true
                    pair.first.error = "This field has an error"
                }
            }
        }

        /*
        * check if remaining fields have no errors
        * */
        belowViews.forEach {
            if (it.text.isNullOrEmpty()) {
                hasErrors = true
                it.error = "This field can't be empty"
            }
        }

        if (hasErrors) {
            Toast.makeText(activity, "You have errors", Toast.LENGTH_SHORT).show()
        } else {
            val pmsAc = actualVal(binding?.etPmsActual!!)
            val agoAc = actualVal(binding?.etAgoActual!!)
            val ikAc = actualVal(binding?.etIkActual!!)

            val eventx = LoadingDialogEvent(
                ikAc, agoAc, pmsAc,
                binding?.etSealRange!!.text.toString(),
                binding?.etBrokenSeal!!.text.toString(),
                binding?.etDeliveryNumber!!.text.toString()
            )

            // push to subscribers
            loadingEvent.onNext(eventx)
        }

    }

    private fun actualVal(editText: EditText): Int? {
        return if (editText.text.isNullOrEmpty()) {
            null
        } else {
            editText.text.toString().toInt()
        }
    }
}