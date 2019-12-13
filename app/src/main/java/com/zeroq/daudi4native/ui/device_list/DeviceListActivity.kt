package com.zeroq.daudi4native.ui.device_list

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.commons.BaseActivity
import com.zeroq.daudi4native.services.BluetoothService
import kotlinx.android.synthetic.main.activity_device_list.*

class DeviceListActivity : BaseActivity() {

    private var mService: BluetoothService? = null
    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null

    companion object {
        val EXTRA_DEVICE_ADDRESS = "device_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)

        /**
         * set adapters
         * */
        paired_devices.adapter = mPairedDevicesArrayAdapter
        paired_devices.onItemClickListener = mDeviceClickListener

        new_devices.adapter = mNewDevicesArrayAdapter
        new_devices.onItemClickListener = mDeviceClickListener

        // Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        /*
        * init bluetooth service
        * */
        mService = BluetoothService(this, null)

        // Get a set of currently paired devices
        val pairedDevices = mService!!.pairedDev

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size > 0) {
            title_paired_devices.visibility = View.VISIBLE

            for (device in pairedDevices) {
                val d = device.name + "\n" + device.address
                mPairedDevicesArrayAdapter!!.add(d)
            }
        } else {
            mPairedDevicesArrayAdapter!!.add("No paired devices")
        }


        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED)

        button_scan.setOnClickListener {
            it.visibility = View.GONE
            doDiscovery()
        }
    }

    private var mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                        mNewDevicesArrayAdapter!!.add(device.name + "\n" + device.address)
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    /**
                     * stop activity progress
                     * */
                    if (mNewDevicesArrayAdapter?.count == 0) {
                        mNewDevicesArrayAdapter?.add("No devices found")
                    }
                }

            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mService != null) {
            mService!!.cancelDiscovery()
        }
        mService = null
        this.unregisterReceiver(mReceiver)
    }


    private fun doDiscovery() {
        /**
         * start scanning indicator
         * */
        title_new_devices.visibility = View.VISIBLE

        if (mService!!.isDiscovering) {
            mService!!.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        mService!!.startDiscovery()

    }

    private var mDeviceClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            // Cancel discovery because it's costly and we're about to connect
            mService?.cancelDiscovery()

            val info = (view as TextView).text.toString()
            val address = info.substring(info.length - 17)

            val intent = Intent()
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address)

            setResult(Activity.RESULT_OK, intent)
            finish()
        }
}
