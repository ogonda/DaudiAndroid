package com.zeroq.daudi4native.ui.loading_order

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.lifecycle.Observer
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.data.models.TruckModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.ui.printing.PrintingActivity
import com.zeroq.daudi4native.utils.ActivityUtil
import com.zeroq.daudi4native.utils.ImageUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_loading_order.*
import kotlinx.android.synthetic.main.toolbar.*
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.toast
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class LoadingOrderActivity : BaseActivity() {

    lateinit var viewModel: LoadingOrderViewModel

    @Inject
    lateinit var imageUtil: ImageUtil

    @Inject
    lateinit var activityUtil: ActivityUtil

    lateinit var _user: UserModel
    lateinit var liveTruck: TruckModel

    companion object {
        const val ID_TRUCK_EXTRA = "IDTRUCK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_order)

        /**
         * hide keyboad
         * */
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        setUpToolbar()
        viewModel = getViewModel(LoadingOrderViewModel::class.java)

        if (intent.hasExtra(ID_TRUCK_EXTRA)) {
            val idTruck = intent.getStringExtra(ID_TRUCK_EXTRA)
            idTruck?.let {
                viewModel.setTruckId(idTruck)
            }
        }

        logic()
        createProgress()
    }

    lateinit var progressDialog: Dialog
    private fun createProgress() {
        progressDialog = Dialog(this)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.custom_progress_dialog)
        progressDialog.setCancelable(false)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "GatePass"
    }

    lateinit var inputs: List<EditText>
    private fun logic() {

        viewModel.getUser().observe(this, Observer {
            if (it.isSuccessful) {
                _user = it.data()!!
                viewModel.setDepotId(_user.config?.depotid.toString())
            } else {
                Timber.e(it.error()!!)
            }
        })

        viewModel.getDepot().observe(this, Observer {
            if (it.isSuccessful) {

                it.data()?.let { depo ->
                    tv_depot_name.text = "[ ${depo.Name} ]"
                }
            } else {
                Timber.e(it.error())
            }
        })

        inputs = listOf(et_seal, et_broken_seals, et_delivery_note)

        inputs.forEach { et ->
            et.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty()) {
                        et.error = null
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.isNullOrEmpty()) {
                        et.error = "This field can't be empty"
                    }
                }
            })
        }

        viewModel.getTruck().observe(this, Observer {
            if (it.isSuccessful) {
                liveTruck = it.data()!!
                initialTruckValues(it.data()!!)

            } else {
                Timber.e(it.error()!!)
            }
        })

    }

    private fun initialTruckValues(truck: TruckModel) {

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm aaa")
        tv_today_date.text = sdf.format(Date()).toUpperCase()


        tv_truck_id.text = truck.truckId

        // driver data
        tv_driver_value.text = truck.drivername
        tv_driver_passport_value.text = truck.driverid
        tv_number_plate_value.text = truck.numberplate
        tv_organisation_value.text = truck.company?.name

        // inputs
        val data = truck.stagedata?.get("4")?.data
        et_delivery_note.setText(data?.deliveryNote)
        et_seal.setText(data?.seals?.range)
        val broken = data?.seals?.broken?.joinToString("-")
        et_broken_seals.setText(broken)


        // fuel
        tv_pms_value.text = truck.fuel?.pms?.qty.toString()
        tv_ago_value.text = truck.fuel?.ago?.qty.toString()
        tv_ik_value.text = truck.fuel?.ik?.qty.toString()

        // qr
        val depotUrl =
            "https://us-central1-emkaybeta.cloudfunctions.net/truckDetail?D=${_user.config?.depotid}&T=${truck.truckId}"

        val dimensions = imageUtil.dpToPx(this, 150)

        val thread = Thread(Runnable {
            val myBitmap = QRCode.from(depotUrl)
                .withSize(dimensions, dimensions)
                .bitmap()

            runOnUiThread {
                iv_qr.setImageBitmap(myBitmap)
            }
        })

        thread.start()

        btnPrint.setOnClickListener {
            if (!validateErrors()) {
                requestPermissions()
            }
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
                    submit()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {

                }
            }).check()
    }

    private fun validateErrors(): Boolean {
        var hasErrors = false

        inputs.forEach {
            if (it.text.isNullOrEmpty()) {
                it.error = "This field can't be empty"
                hasErrors = true
            }
        }

        return hasErrors
    }


    private fun submit() {
        progressDialog.show()

        viewModel.updateSeals(
            et_seal.text.toString(), et_broken_seals.text.toString(),
            et_delivery_note.text.toString()
        ).observe(this, Observer {
            if (it.isSuccessful) {
                print()
            } else {
                progressDialog.hide()
                toast("An error occurred while submitting seals")
                Timber.e(it.error())
            }
        })
    }


    var saveImageSub: Disposable? = null
    private fun print() {
        activityUtil.disableViews(layout_constraint)
        btnPrint?.isEnabled = true

        hideButton(true)

        // clear to avoid leaks
        saveImageSub?.dispose()
        saveImageSub = null

        saveImageSub = imageUtil.reactiveTakeScreenShot(content_scroll)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                progressDialog.hide()

                if (it) {
                    hideButton(false)
                    PrintingActivity.startPrintingActivity(
                        this,
                        _user.config?.depotid.toString(), liveTruck.Id!!,
                        "3",
                        liveTruck.config?.sandbox!!
                    )
                } else {
                    hideButton(false)
                    toast("Sorry an error occurred")
                }
            }
    }


    private fun hideButton(hide: Boolean) {
        if (hide) {
            btnPrint.visibility = View.GONE
        } else {
            btnPrint.visibility = View.VISIBLE
        }
    }
}
