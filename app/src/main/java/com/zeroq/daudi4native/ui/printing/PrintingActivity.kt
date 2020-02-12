package com.zeroq.daudi4native.ui.printing

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.data.models.UserModel
import com.zeroq.daudi4native.services.BluetoothService
import com.zeroq.daudi4native.ui.device_list.DeviceListActivity
import com.zeroq.daudi4native.utils.PrintPic
import kotlinx.android.synthetic.main.activity_printing.*
import kotlinx.android.synthetic.main.activity_truck_detail.*
import org.jetbrains.anko.toast
import timber.log.Timber
import kotlin.experimental.and
import kotlin.experimental.or

class PrintingActivity : BaseActivity() {

    lateinit var printingViewModel: PrintingViewModel

    private var mService: BluetoothService? = null
    private var con_dev: BluetoothDevice? = null

    private var orderId: String? = null
    private var stage: String? = null


    private var user: UserModel? = null

    companion object {
        private const val REQUEST_ENABLE_BT = 2
        private const val REQUEST_CONNECT_DEVICE = 1


        fun startPrintingActivity(
            context: Context,
            orderId: String,
            stage: String
        ) {
            val intent = Intent(context, PrintingActivity::class.java)
            intent.putExtra("ORDERID", orderId)
            intent.putExtra("STAGE", stage)

            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printing)



        printingViewModel = getViewModel(PrintingViewModel::class.java)

        if (intent.extras != null) {
            orderId = intent.getStringExtra("ORDERID")
            stage = intent.getStringExtra("STAGE")


        }

        printingViewModel.getUser().observe(this, Observer {
            if (it.isSuccessful) {
                user = it.data()
            } else {
                Timber.e(it.error()!!)
            }
        })


        setSupportActionBar(toolbar as Toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close)

        if (stage == "1") {
            supportActionBar!!.title = "Printing Loading Order"
        } else {
            supportActionBar!!.title = "Printing GatePass"
        }

        /*
        * bluetooth service
        * */
        checkBluetoothState()
        operationBtns()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                cleanUp()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()

        /**
         * check if bluetooth is on
         * */
        if (mService!!.isBTopen) {
            btnClose?.isEnabled = true
            btnSearch?.isEnabled = true

        } else {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)

            btnClose?.isEnabled = false
            btnSearch?.isEnabled = false
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

    private fun operationBtns() {
        btn_print?.setOnClickListener(clickEvent())
        btnSearch?.setOnClickListener(clickEvent())
        btnClose?.setOnClickListener(clickEvent())
        btnSandbox?.setOnClickListener(clickEvent())
    }

    private fun checkBluetoothState() {
        mService = BluetoothService(this, Handler { msg ->
            when (msg.what) {
                BluetoothService.MESSAGE_STATE_CHANGE -> when (msg.arg1) {
                    BluetoothService.STATE_CONNECTED -> {
                        toast("Connect successful")
                        btnClose!!.isEnabled = true
                        btn_print!!.isEnabled = true
                    }
                    BluetoothService.STATE_CONNECTING -> {
                        Timber.d("Connecting")
                    }

                    BluetoothService.STATE_LISTEN, BluetoothService.STATE_NONE -> {
                        Timber.d("State None")
                    }
                }


                BluetoothService.MESSAGE_CONNECTION_LOST -> {
                    toast("Device connection was lost")

                    btnClose?.isEnabled = false
                    btn_print?.isEnabled = false
                }
                BluetoothService.MESSAGE_UNABLE_CONNECT -> {
                    Toast.makeText(
                        applicationContext, "Unable to connect device",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            return@Handler true
        })


        if (!mService!!.isAvailable) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    internal inner class clickEvent : View.OnClickListener {
        override fun onClick(v: View) {
            when (v) {
                btnSearch -> {
                    val intent = Intent(this@PrintingActivity, DeviceListActivity::class.java)
                    startActivityForResult(intent, REQUEST_CONNECT_DEVICE)
                }

                btnClose -> {
                    cleanUp()
                }

                btn_print -> {
                    databasePrintTransactions()

                    Thread {
                        startPrintingProcess()
                    }.start()
                }

                btnSandbox -> {
                    databasePrintTransactions()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    btnSearch.isEnabled = true
                    toast("Bluetooth open successful")
                } else {
                    finish()
                }
            }

            REQUEST_CONNECT_DEVICE -> {

                if (resultCode == Activity.RESULT_OK) {
                    val address = data?.extras!!
                        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)

                    con_dev = mService!!.getDevByMac(address)

                    // connect
                    mService!!.connect(con_dev)
                }
            }
        }

    }

    private fun startPrintingProcess() {
        val msg = "https://emkaynow.com"
        val lang = getString(R.string.strLang)

        printImage()

        val cmd = ByteArray(3)
        cmd[0] = 0x1b
        cmd[1] = 0x21

        if (lang.compareTo("en") == 0) {
            cmd[2] = cmd[2] or 0x10
            mService!!.write(cmd)
            mService!!.sendMessage("", "GBK")
            cmd[2] = cmd[2] and 0xEF.toByte()
            mService!!.write(cmd)
            mService!!.sendMessage(msg + "\n\n", "GBK")
        }

        Handler(Looper.getMainLooper()).post {
            toast("Wait for the receipt to print")
        }
    }

    @SuppressLint("SdCardPath")
    private fun printImage() {
        val sendData: ByteArray?
        val pg = PrintPic()
        pg.initCanvas(537)
        pg.initPaint()


        // path
        @Suppress("DEPRECATION")
        val pathDir = Environment.getExternalStorageDirectory().absolutePath + "/Emkaynow/0.png"

        pg.drawImage(
            0f,
            0f,
            pathDir
        )
        sendData = pg.printDraw()
        mService!!.write(sendData)
    }

    private fun cleanUp() {
        mService?.stop()
        mService = null


        finish()
    }


    private fun databasePrintTransactions() {
        if (stage == "1") {
            printingViewModel.setLoadingPrintedState(user!!, orderId!!)
                .observe(this@PrintingActivity, Observer {
                    if (!it.isSuccessful) {
                        Timber.e(it.error())
                    } else {
                        toast("written to database, wait for receipt")
                    }
                })
        }


        if (stage == "3") {
            printingViewModel.setGatePassPrintedState(user!!, orderId!!)
                .observe(this@PrintingActivity, Observer {
                    if (!it.isSuccessful) {
                        Timber.e(it.error())
                    } else {
                        toast("written to database, wait for receipt")
                    }
                })
        }
    }
}
