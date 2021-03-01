package com.zeroq.daudi4native.ui.truck_detail

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.data.models.*
import com.zeroq.daudi4native.databinding.ActivityTruckDetailBinding
import com.zeroq.daudi4native.ui.printing.PrintingActivity
import com.zeroq.daudi4native.utils.ActivityUtil
import com.zeroq.daudi4native.utils.ImageUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.toast
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class TruckDetailActivity : BaseActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var imageUtil: ImageUtil

    @Inject
    lateinit var activityUtil: ActivityUtil

    lateinit var binding: ActivityTruckDetailBinding

    lateinit var truckDetailViewModel: TruckDetailViewModel

    private val _fuelTypeList = ArrayList<String>()


    // set data to compartments
    private lateinit var viewComp: List<EditText>

    private lateinit var btnComp: List<AppCompatButton>
    private lateinit var _topInputs: List<EditText>

    private lateinit var _user: UserModel
    // private var DepotTruck: TruckModel? = null
    private var depotOrder: OrderModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTruckDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        /**
         * hide keyboad
         * */
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        viewComp = listOf(
            binding.etC1Qty, binding.etC2Qty,
            binding.etC3Qty, binding.etC4Qty, binding.etC5Qty, binding.etC6Qty, binding.etC7Qty
        )

        btnComp = listOf(
            binding.etC1Type, binding.etC2Type,
            binding.etC3Type, binding.etC4Type, binding.etC5Type, binding.etC6Type, binding.etC7Type
        )

        _topInputs = listOf(binding.etDriverName, binding.etDriverId, binding.etDriverPlate)

        /*
        * set  the viewModel
        * */
        truckDetailViewModel = getViewModel(TruckDetailViewModel::class.java)
        intent.getStringExtra("ORDER_ID")?.let { id ->
            truckDetailViewModel.setOrderId(id)
        }


        truckDetailViewModel.getUser().observe(this, Observer {
            if (it.isSuccessful) {
                _user = it.data()!!
                truckDetailViewModel.setUserModel(_user);
                truckDetailViewModel.setDepotId(_user.config?.app?.depotid.toString())
            } else {
                Timber.e(it.error()!!)
            }
        })

        truckDetailViewModel.getDepot().observe(this, Observer {
            if (it.isSuccessful) {

                it.data()?.let { depo ->
                    binding.tvDepotNameD.text = "[ ${depo.Name} ]"
                }
            } else {
                Timber.e(it.error())
            }
        })

        truckDetailViewModel.getOrder().observe(this, Observer {
            if (it.isSuccessful) {
                depotOrder = it.data()
                initialOrderValues(it.data()!!)
            } else {
                Timber.e(it.error())
            }
        })




        initToolbar()
        topInputs()
        compartimentsButtonOps()
        compartmentsInputs()
        initProgress()
    }


    private fun initialOrderValues(order: OrderModel) {

        _fuelTypeList.clear()
        _fuelTypeList.add("EMPTY")

        if (order.fuel?.pms?.qty != 0) _fuelTypeList.add("PMS")
        if (order.fuel?.ago?.qty != 0) _fuelTypeList.add("AGO")
        if (order.fuel?.ik?.qty != 0) _fuelTypeList.add("IK")

        binding.tvTruckId.text = order.QbConfig?.InvoiceNumber
        
        binding.tvCustomerValue.text = order.customer?.name
        binding.etDriverName.setText(order.truck?.driverdetail?.name)
        binding.etDriverId.setText(order.truck?.driverdetail?.id)
        binding.etDriverPlate.setText(order.truck?.truckdetail?.numberplate)

        // fuel
        binding.tvPms.text = "PMS [ " + order.fuel?.pms?.qty + " ]"
        binding.tvAgo.text = "AGO [ " + order.fuel?.ago?.qty + " ]"
        binding.tvIk.text = "IK      [ " + order.fuel?.ik?.qty + " ]"


        // fuel entries


        binding.tvPmsEntry.text = getBatchName(order.fuel?.pms!!)
        binding.tvAgoEntry.text = getBatchName(order.fuel?.ago!!)
        binding.tvIkEntry.text = getBatchName(order.fuel?.ik!!)




        order.truck?.compartments?.forEachIndexed { index, compartment ->
            if (compartment.qty != null && compartment.qty != 0) {
                btnComp[index].text = compartment.fueltype?.toUpperCase()
                viewComp[index].setText(compartment.qty!!.toString())
            } else {
                btnComp[index].text = "EMPTY"
                viewComp[index].text = null
                viewComp[index].hint = "Empty Comp"
                viewComp[index].isEnabled = false
            }
        }

        binding.tvAuthByValue.text = order.truckStageData!!["1"]?.user?.name

        // display name
        binding.tvConfirmedByValue.text = firebaseAuth.currentUser?.displayName


        /**
         * current data, will change it later to be regenerated before printing
         * */
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm aaa")
        binding.tvTodayDate.text = sdf.format(Date()).toUpperCase()

        /**
         * create qr
         * */


        val depotUrl = "https://daudi.africa/orders/${depotOrder?.Id}"

        val dimensions = imageUtil.dpToPx(this, 150)

        val thread = Thread(Runnable {
            val myBitmap = QRCode.from(depotUrl)
                .withSize(dimensions, dimensions)
                .bitmap()

            runOnUiThread {
                binding.qr.setImageBitmap(myBitmap)
            }
        })

        thread.start()

        binding.btnPrint.setOnClickListener {
            progressDialog.show()
            requestPermissions()
        }

        /**
         * disable views if the truck is already printed
         * */
        if (order.printStatus?.LoadingOrder?.status != null
            && order.printStatus?.LoadingOrder?.status!!
        ) {
            activityUtil.disableViews(binding.layoutConstraint)
            binding.btnPrint.isEnabled = true
        }

    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar.toolbar)
    }

    private lateinit var progressDialog: Dialog
    private fun initProgress() {
        progressDialog = Dialog(this@TruckDetailActivity)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.custom_progress_dialog)
        progressDialog.setCancelable(false)
    }


    private fun topInputs() {
        _topInputs.forEach {
            it.filters = arrayOf<InputFilter>(InputFilter.AllCaps())

            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    if (s.isNullOrEmpty()) {
                        it.error = "This field cant be blank"
                    } else {
                        it.error = null
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrEmpty()) {
                        it.error = "This field cant be blank"
                    } else {
                        it.error = null
                    }
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.isNullOrEmpty()) {
                        it.error = "This field cant be blank"
                    } else {
                        it.error = null
                    }
                }
            })
        }
    }


    private fun compartimentsButtonOps() {
        /**
         * make sure the compartment values tally with the given fuel
         * */

        btnComp.forEachIndexed { i, btn ->
            btn.setOnClickListener {
                var index = _fuelTypeList.indexOf(btn.text)
                index++


                if (index > (_fuelTypeList.size - 1)) {
                    index = 0

                    viewComp[i].text = null
                    viewComp[i].hint = "Empty Comp"
                    viewComp[i].error = null

                    /**
                     * disable
                     * */
                    viewComp[i].isEnabled = false
                } else {
                    viewComp[i].hint = "Enter Amount"
                    viewComp[i].isEnabled = true
                }

                btn.text = _fuelTypeList[index]
            }
        }
    }

    private fun compartmentsInputs() {

        viewComp.forEach {
            it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (it.isEnabled && (s.isNullOrEmpty() || s.toString().toInt() < 500)) {
                        it.error = "Should contain more than 500L"
                    } else {
                        if (it.isEnabled && (s.isNullOrEmpty() || s.toString().toInt() > 30000)) {
                            it.error = "Can't carry more than 30000L"
                        } else {
                            it.error = null
                        }
                    }
                }
            })
        }
    }


    private fun getBatchName(batches: Batches): String? {
        return if (!batches.entries.isNullOrEmpty()) {
            val entrySize = batches.entries!!.size
            batches.entries!![entrySize - 1].name
        } else {
            "****************"
        }
    }


    private fun requestPermissions() {
        Dexter.withActivity(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    validateAndPost()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {

                }
            }).check()
    }


    private fun validateAndPost() {
        /**
         * check top inputs
         * */
        var inputErrors = false

        _topInputs.forEach {
            if (it.text.toString().isNullOrEmpty()) {
                inputErrors = true

                it.error = "This field cant be blank"
            }
        }


        /**
         * check if compartments are fine
         *
         * local fuel inputs
         * */

        var pmsLocal = depotOrder?.fuel?.pms?.qty!!
        var agoLocal = depotOrder?.fuel?.ago?.qty!!
        var ikLocal = depotOrder?.fuel?.ik?.qty!!


        // from buttons
        btnComp.forEachIndexed { index, appCompatButton ->
            when (appCompatButton.text) {
                "PMS" ->
                    if (!viewComp[index].text.isNullOrEmpty()) {
                        pmsLocal -= viewComp[index].text.toString().toInt()
                    } else {
                        inputErrors = true
                        viewComp[index].error = "Field can't be empty"
                    }


                "AGO" ->
                    if (!viewComp[index].text.isNullOrEmpty()) {
                        agoLocal -= viewComp[index].text.toString().toInt()
                    } else {
                        inputErrors = true
                        viewComp[index].error = "Field can't be empty"
                    }

                "IK" ->

                    if (!viewComp[index].text.isNullOrEmpty()) {
                        ikLocal -= viewComp[index].text.toString().toInt()
                    } else {
                        inputErrors = true
                        viewComp[index].error = "Field can't be empty"
                    }
            }
        }


        // check if the local fuel is zero and no error
        if (pmsLocal == 0 && agoLocal == 0 && ikLocal == 0 && !inputErrors) {
            pushToServer()
        } else {

            progressDialog.dismiss()

            toast("Make sure you have no errors")

            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                vibrator.run {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrate(
                            VibrationEffect.createOneShot(
                                500,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrate(500)
                    }
                } // for 500 ms
            }
        }

    }


    private fun pushToServer() {
        val compList = ArrayList<Compartment>()

        btnComp.forEachIndexed { index, appCompatButton ->

            when (val btnValue: String = appCompatButton.text.toString()) {
                "EMPTY" ->
                    compList.add(Compartment(index, null, null))

                else ->
                    compList.add(
                        Compartment(
                            index,
                            btnValue.toLowerCase(),
                            viewComp[index].text.toString().toInt()
                        )
                    )
            }
        }

        val driverName = binding.etDriverName.text.toString().toUpperCase()
        val driverId = binding.etDriverId.text.toString().toUpperCase()
        val numberPlate = binding.etDriverPlate.text.toString().toUpperCase()

        truckDetailViewModel.updateCompartmentAndDriver(
            _user,
            depotOrder?.Id!!,
            compList,
            driverId,
            driverName,
            numberPlate
        ).observe(this, Observer {

            if (it.isSuccessful) {
                // create an image to print
                cleanPageForPrinting()
            } else {
                progressDialog.dismiss()
                Snackbar.make(binding.layoutConstraint, "An error occurred", Snackbar.LENGTH_SHORT).show()
                Timber.e(it.error()!!)

            }
        })
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }


    var saveImageSub: Disposable? = null
    private fun cleanPageForPrinting() {
        hideButton(false)
        activityUtil.disableViews(binding.layoutConstraint)

        /**
         * Take screenshot now
         * */

        // clear to avoid leaks
        saveImageSub?.dispose()
        saveImageSub = null

        saveImageSub = imageUtil.reactiveTakeScreenShot(binding.contentScroll)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    hideButton(true)

                    progressDialog.dismiss()


                    PrintingActivity.startPrintingActivity(
                        this,
                        depotOrder?.Id!!,
                        "1"
                    )
                } else {
                    /**
                     * An error occured
                     * */
                    progressDialog.dismiss()

                    Toast.makeText(this, "Sorry an error occurred", Toast.LENGTH_SHORT).show()
                    hideButton(true)
                }
            }
    }


    private fun hideButton(visible: Boolean) {
        if (visible) {
            binding.btnPrint.visibility = View.VISIBLE
        } else {
            binding.btnPrint.visibility = View.GONE
        }
    }

}
