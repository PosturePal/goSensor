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

                    deviceFr.mAvailableDevicesAdapter.notifyDataSetChanged()

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
//                broadcastUpdate(intentAction);
                    deviceFr.mAvailableDevicesAdapter.notifyDataSetChanged()

                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {

            }



//            fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//                if (status != BluetoothGatt.GATT_SUCCESS) {
//                    // Handle the error
//                    return
//                }
//
//                // Get the counter characteristic
//                val characteristic = gatt
//                        .getService(SERVICE_UUID)
//                        .getCharacteristic(CHARACTERISTIC_COUNTER_UUID)
//
//                // Enable notifications for this characteristic locally
//                gatt.setCharacteristicNotification(characteristic, true)
//
//                // Write on the config descriptor to be notified when the value changes
//                val descriptor = characteristic.getDescriptor(DESCRIPTOR_CONFIG_UUID)
//                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                gatt.writeDescriptor(descriptor)
//            }


        }
        private var connected: Boolean = false

        init {
            this.deviceName = row?.findViewById(R.id.device_item_name)
            this.deviceStatus = row?.findViewById(R.id.device_item_status)
            val connectBtn = row?.findViewById<Button>(R.id.device_connect_button)

            connectBtn?.setOnClickListener { view ->

                connected = !connected
                if (connected && device != null) {
                    connected = this.startConnection()


                    if(connected)
                    {
                        alert(view, "Connecting device with address " + this.device?.address + " " + connected)
                        connectBtn.alpha = 0.5f
                    }
                } else {
//                    mBluetoothGatt?.close()
//                    mBluetoothGatt?.disconnect()
                    alert(view, "Disconnected device with address " + this.deviceAddress)
                    connectBtn.alpha = 1.0f
                }
            }
        }

        fun startConnection(): Boolean {
            device?.createBond()

            mBluetoothGatt = device?.connectGatt(deviceFr.context, false, mGattCallback)
            return true
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
        viewHolder.deviceStatus?.text =
                when (device.bondState) {
                    BluetoothDevice.BOND_NONE -> "not paired"
                    BluetoothDevice.BOND_BONDING -> "pairing"
                    BluetoothDevice.BOND_BONDED -> "paired before"
                    else -> device.address
                }
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