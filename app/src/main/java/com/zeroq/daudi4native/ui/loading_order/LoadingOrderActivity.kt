package com.zeroq.daudi4native.ui.loading_order

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.internal.ViewUtils.dpToPx
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.adapters.UploadNotesAdapter
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.data.models.DepotModel
import com.zeroq.daudi4native.data.models.OrderModel
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.databinding.ActivityLoadingOrderBinding
import com.zeroq.daudi4native.databinding.ToolbarBinding
import com.zeroq.daudi4native.ui.preview.PreviewActivity
import com.zeroq.daudi4native.ui.printing.PrintingActivity
import com.zeroq.daudi4native.utils.ActivityUtil
import com.zeroq.daudi4native.utils.AdapterSpacer
import com.zeroq.daudi4native.utils.ImageUtil
import com.zeroq.daudi4native.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.glxn.qrgen.android.QRCode
import org.jetbrains.anko.toast
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class LoadingOrderActivity : BaseActivity() {

    lateinit var viewModel: LoadingOrderViewModel

    lateinit var binding: ActivityLoadingOrderBinding

    @Inject
    lateinit var imageUtil: ImageUtil

    @Inject
    lateinit var activityUtil: ActivityUtil

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var storageReference: StorageReference


    lateinit var _user: UserModel
    lateinit var liveOrder: OrderModel
    private var depot: DepotModel? = null


    private val REQUEST_CAPTURE_IMAGE: Int = 500

    val compositeDisposable = CompositeDisposable()

    /**
     * global adapter
     * */
    lateinit var adapter: UploadNotesAdapter

    companion object {
        const val ID_ORDER_EXTRA = "ORDERID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingOrderBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        /**
         * hide keyboard
         * */
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        setUpToolbar()
        viewModel = getViewModel(LoadingOrderViewModel::class.java)

        if (intent.hasExtra(ID_ORDER_EXTRA)) {
            val idTruck = intent.getStringExtra(ID_ORDER_EXTRA)
            idTruck?.let {
                viewModel.setOrderId(idTruck)
            }
        }

        logic()
        createProgress()
        setupAdapter()

    }

    private fun setupAdapter() {
        /*
               * add recycler adapter
               * */
        adapter = UploadNotesAdapter(storageReference)
        binding.notesRecycler.adapter = adapter
        binding.notesRecycler!!.layoutManager = LinearLayoutManager(
            applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.notesRecycler.addItemDecoration(AdapterSpacer(6, utils.dpToPx(5, resources), true))

        /*
        * test data
        * */
        val notes = ArrayList<Pair<Boolean, String>>()
        notes.add(Pair(true, ""))

        adapter.replaceDeliveryNotes(notes)

        // consume events from adapter
        val startCamSub = adapter.startCamera.subscribe {
            takePicture()
        }

        val imageClicked = adapter.onClick.subscribe {
            PreviewActivity.startPreviewActivity(this, it.second)
        }

        val imageLongClick = adapter.onLongPress.subscribe {
            removeImage(it.second)
        }

        // add to a class of disponsing subscribers
        compositeDisposable.add(startCamSub)
        compositeDisposable.add(imageClicked)
        compositeDisposable.add(imageLongClick)
    }

    lateinit var progressDialog: Dialog
    private fun createProgress() {
        progressDialog = Dialog(this)
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setContentView(R.layout.custom_progress_dialog)
        progressDialog.setCancelable(false)
    }

    private fun setUpToolbar() {
        lateinit var toolbar: Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "GatePass"
    }

    lateinit var inputs: List<EditText>
    private fun logic() {

        viewModel.getUser().observe(this, Observer {
            if (it.isSuccessful) {
                _user = it.data()!!
                viewModel.setSwitchUser(_user)
                viewModel.setDepotId(_user.config?.app?.depotid.toString())
            } else {
                Timber.e(it.error()!!)
            }
        })

        viewModel.getDepot().observe(this, Observer {
            if (it.isSuccessful) {
                depot = it.data()
                it.data()?.let { depo ->
                    binding.tvDepotName.text = "[ ${depo.Name} ]"
                }
            } else {
                depot = null
                Timber.e(it.error())
            }
        })

        inputs = listOf(binding.etSeal, binding.etBrokenSeals, binding.etDeliveryNote)

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

        viewModel.getOrder().observe(this, Observer {
            if (it.isSuccessful) {
                liveOrder = it.data()!!
                initialOrderValues(it.data()!!)

                /**
                 * set adapters data
                 * */
                val pathTemp = ArrayList<Pair<Boolean, String>>()
                it.data()?.deliveryNote?.photos?.let { paths ->
                    if (paths.size < 5) pathTemp.add(Pair(true, ""))
                    paths.forEach { v -> pathTemp.add(Pair(false, v)) }
                }

                adapter.replaceDeliveryNotes(pathTemp)


            } else {
                Timber.e(it.error())
            }
        })

    }

    private fun initialOrderValues(orderModel: OrderModel) {

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm aaa")
        binding.tvTodayDate.text = sdf.format(Date()).toUpperCase()


        binding.tvTruckId.text = orderModel.QbConfig?.InvoiceNumber

        // driver data
        binding.tvDriverValue.text = orderModel.truck?.driverdetail?.name
        binding.tvDriverPassportValue.text = orderModel.truck?.driverdetail?.id
        binding.tvNumberPlateValue.text = orderModel.truck?.truckdetail?.numberplate
        binding.tvOrganisationValue.text = orderModel.customer?.name

        // inputs
        val seals = orderModel.seals


        binding.etDeliveryNote.setText(orderModel?.deliveryNote?.value)
        binding.etSeal.setText(seals?.range?.joinToString("-"))
        binding.etBrokenSeals.setText(seals?.broken?.joinToString("-"))


        // fuel
        binding.tvPmsValue.text = orderModel.fuel?.pms?.qty.toString()
        binding.tvAgoValue.text = orderModel.fuel?.ago?.qty.toString()
        binding.tvIkValue.text = orderModel.fuel?.ik?.qty.toString()

        val depotUrl = "https://daudi.africa/orders/${orderModel.Id}"

        val dimensions = imageUtil.dpToPx(this, 170)

        val thread = Thread(Runnable {
            val myBitmap = QRCode.from(depotUrl)
                .withSize(dimensions, dimensions)
                .bitmap()

            runOnUiThread {
                binding.ivQr.setImageBitmap(myBitmap)
            }
        })

        thread.start()

        binding.btnPrint.setOnClickListener {
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
            _user, liveOrder.Id!!,
            binding.etSeal.text.toString(),
            binding.etBrokenSeals.text.toString(),
            binding.etDeliveryNote.text.toString()
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
        activityUtil.disableViews(binding.layoutConstraint)
        binding.btnPrint?.isEnabled = true

        hideButton(true)

        // clear to avoid leaks
        saveImageSub?.dispose()
        saveImageSub = null

        saveImageSub = imageUtil.reactiveTakeScreenShot(binding.contentScroll)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                progressDialog.hide()

                if (it) {
                    hideButton(false)
                    PrintingActivity.startPrintingActivity(this, liveOrder.Id!!, "3")
                } else {
                    hideButton(false)
                    toast("Sorry an error occurred")
                }
            }
    }


    fun takePicture() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (pictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.extras != null) {
                val imageBitMap = data.extras?.get("data") as Bitmap;
                binding.ivMkLogo.setImageBitmap(imageBitMap)
                uploadImage(imageBitMap)
            }
        }
    }

    private fun removeImage(path: String) {
        viewModel.removeDeliveryNotePath(_user, liveOrder.Id!!, path)
            .observe(this, Observer {
                if (it.isSuccessful) {
                    toast("photo removed")
                } else {
                    toast("Photo was not removed because of an error")
                }
            })
    }

    private fun uploadImage(bitmap: Bitmap) {
        val upload = viewModel.uploadNote(bitmap, liveOrder)

        upload.first.addOnSuccessListener {
            viewModel.addDeliveryNotePath(_user, liveOrder.Id!!, upload.second.path)
                .observe(this, Observer {
                    if (it.isSuccessful) {
                        toast("photo added")
                    } else {
                        toast("Error:: uploading photo")
                    }
                })

        }.addOnFailureListener {
            toast("An error occurred nothing was uploaded")
            Timber.e(it)
        }

    }


    private fun hideButton(hide: Boolean) {
        if (hide) {
            binding.btnPrint.visibility = View.GONE
            binding.notesRecycler.visibility = View.GONE
        } else {
            binding.btnPrint.visibility = View.VISIBLE
            binding.notesRecycler.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}