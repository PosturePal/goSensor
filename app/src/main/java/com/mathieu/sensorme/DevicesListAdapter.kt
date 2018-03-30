package com.mathieu.sensorme

import android.bluetooth.*
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.mathieu.sensorme.fragments.DevicesFragment
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGatt
import java.util.*
import android.bluetooth.BluetoothGattService
import android.net.wifi.aware.Characteristics
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.collections.ArrayList
import kotlin.experimental.and


class DevicesListAdapter(private var deviceFr: DevicesFragment, private var items: ArrayList<BluetoothDevice>) : BaseAdapter() {
    private class ViewHolder(private val deviceFr: DevicesFragment, row: View?) {
        var deviceName: TextView? = null
        var deviceStatus: TextView? = null
        var deviceAddress: String = ""
        var device: BluetoothDevice? = null

        val TAG = "ViewHolder List"
        private val STATE_DISCONNECTED = 0
        private val STATE_CONNECTING = 1
        private val STATE_CONNECTED = 2


        private var mBluetoothDeviceAddress: String? = null
        private var mBluetoothGatt: BluetoothGatt? = null
        private var mConnectionState: Int = STATE_DISCONNECTED


        val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
        // Various callback methods defined by the BLE API.

        var mChars: ArrayList<BluetoothGattCharacteristic> = ArrayList()
        private final val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int,
                                                 newState: Int) {
                var intentAction: String
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
//                broadcastUpdate(intentAction);
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt?.discoverServices())

                    mBluetoothGatt?.discoverServices()
//                    val chars = mBluetoothGatt?.getService(UUID.randomUUID())?.characteristics


//                    deviceFr.mAvailableDevicesAdapter.notifyDataSetChanged()

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
//                broadcastUpdate(intentAction);
//                    deviceFr.mAvailableDevicesAdapter.notifyDataSetChanged()

                } else {
                    Log.i(TAG, "WHAT IS G ON " + newState)
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                Log.i(TAG, "onCharacteristicChanged\nchar uudi:" + characteristic?.uuid)
                gatt?.readCharacteristic(characteristic)

            }

            override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                val str = toHex(characteristic?.value!!)
                Log.i(TAG, "OnCharREAD: " + str + " ch = " + characteristic.service)
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                // now we cat start reading / writing chars

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    deviceFr.alert("gatt success")

                    for (gattService in gatt!!.getServices()) {
//                        Log.i(TAG, "onServicesDiscovered: ---------------------")
                        Log.i(TAG, "onServicesDiscovered: service=" + gattService.uuid)
                        for (characteristic in gattService.characteristics) {
//                            if (characteristic.uuid.toString() == "0000fff2-0000-1000-8000-00805f9b34fb") {
                            // acc / gyro

//                                Log.i(TAG, "YRRE\n\n\n\t HERE IT IS ACC \n\n")

                            mChars.add(characteristic)

                            Log.i(TAG, "new char add! size: " + mChars.size)
                            mBluetoothGatt?.readCharacteristic(characteristic)
                            mBluetoothGatt?.setCharacteristicNotification(characteristic, true)
//                            }
                        }
                    }
                } else {
                    deviceFr.alert("gatt fuck")
                }
            }

            fun hexStringToByteArray(s: String): ByteArray {
                val len = s.length
                val data = ByteArray(len / 2)
                var i = 0
                while (i < len) {
                    data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
                    i += 2
                }
                return data
            }

            fun toHex(bytes: ByteArray): String {
                val HEX_DIGITS = "0123456789abcdef".toCharArray()
                val c = CharArray(bytes.size * 2)
                var index = 0
                for (b in bytes) {
                    c[index++] = HEX_DIGITS[(b.toInt() shr 4) and 0xf]
                    c[index++] = HEX_DIGITS[b.toInt() and 0xf]
                }
                return String(c)
            }


        }
        private var connected: Boolean = false

        init {
            this.deviceName = row?.findViewById(R.id.device_item_name)
            this.deviceStatus = row?.findViewById(R.id.device_item_status)
            val connectBtn = row?.findViewById<Button>(R.id.device_connect_button)
            val readBtn = row?.findViewById<Button>(R.id.device_read_button)

            connectBtn?.setOnClickListener { view ->

                connected = !connected

//                if (connected && device != null) {


                connected = this.startConnection()
                if (connected) {
                    alert(view, "Connecting device with address " + this.device?.address + " " + connected)
                    connectBtn.alpha = 0.5f
                }
//                } else {
//                    // CONNECTED
//                    //  mBluetoothGatt?.close()
////                    mBluetoothGatt?.disconnect()
////                    alert(view, "Disconnected device with address " + this.deviceAddress)
////                    connectBtn.alpha = 1.0f
//                }
            }

            readBtn?.setOnClickListener { view -> read() }

        }

        fun read()
        {
            Log.i(TAG, "Mchars size: " + mChars.size)

            val services = mBluetoothGatt?.discoverServices()
//
//            for (s in services!!)
//            {
//                for(c in s.characteristics)
//                {
//                    mBluetoothGatt?.readCharacteristic(c)
//                }
//            }
//            for (s in services!!)
//            {
//                for(c in s.characteristics)
//                {
//                    mBluetoothGatt?.readCharacteristic(c)
//                }
//            }

//            if (!mChars.isEmpty()) {
//                for (mChar in mChars) {
//                    mBluetoothGatt?.readCharacteristic(mChar)
//                }
//                deviceFr.alert("readed chars")
//                connected = true
//            } else {
//                deviceFr.alert("no mchar")
////                        mBluetoothGatt?.close()
//            }
        }
        fun startConnection(): Boolean {
            if (device == null) {
                Log.i(TAG, "No device to start a conn")
                return false;
            }
            device?.createBond()
            mBluetoothGatt = device?.connectGatt(deviceFr.context, false, mGattCallback)
            return device?.bondState == BluetoothDevice.BOND_BONDED
        }


        private fun alert(v: View, txt: String) {

            Snackbar.make(v, txt, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View?
        val viewHolder: ViewHolder


        if (convertView == null) {
            view = deviceFr.layoutInflater.inflate(R.layout.device_item, null)
            viewHolder = ViewHolder(deviceFr, view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }


        val device = items[position]
        viewHolder.device = device
        viewHolder.deviceName?.text = device.name
        viewHolder.deviceStatus?.text = device.address
//                when (device.bondState) {
//                    BluetoothDevice.BOND_NONE -> "not paired"
//                    BluetoothDevice.BOND_BONDING -> "pairing"
//                    BluetoothDevice.BOND_BONDED -> "paired before"
//                    else -> device.address
//                }
        viewHolder.deviceAddress = device.address
        return view as View
    }

    override fun getItem(i: Int): Any {
        return items[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun isEmpty(): Boolean {
        return items.isEmpty()
    }

    public fun updateItems(list: ArrayList<BluetoothDevice>) {
        items = list
        notifyDataSetChanged()
    }

    public fun addItem(btd: BluetoothDevice) {
        if (!items.contains(btd)) {
            items.add(btd)
            notifyDataSetChanged()
        }
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }
}