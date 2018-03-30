/*
 * Copyright (C) 2013 The Android Open Source Project
 * Modified 2016 by Samsung Electronics Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mathieu.sensorme

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.UUID

@SuppressLint("Registered")
/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
class BluetoothLeService : Service() {

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED

    private var mHeartRateForArtikCloud: Int? = null

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                broadcastUpdate(intentAction)
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt!!.discoverServices())

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    private val mBinder = LocalBinder()

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     *
     * @return A `List` of supported services.
     */
    val supportedGattServices: List<BluetoothGattService>?
        get() = if (mBluetoothGatt == null) null else mBluetoothGatt!!.services

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String,
                                characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
            val flag = characteristic.properties
            var format = -1
            if (flag and 0x01 != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16
                Log.d(TAG, "Heart rate format UINT16.")
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8
                Log.d(TAG, "Heart rate format UINT8.")
            }
            val heartRate = characteristic.getIntValue(format, 1)!!
            Log.d(TAG, String.format("Received heart rate: %d", heartRate))

            intent.putExtra(EXTRA_DATA, heartRate.toString())
            sendBroadcast(intent)

            mHeartRateForArtikCloud = heartRate

            sendHeartRateToArtikCloud()
        }
        // Comment out the original code that sends other measurement data to UI so that
        // only heart rate data is sent to UI and then to ARTIK Cloud
        //        else {
        //            // For all other profiles, writes the data formatted in HEX.
        //            Log.d(TAG, "broadcastUpdate(action, characteristic): characteristics.getUuid = " + characteristic.getUuid());
        //
        //            final byte[] data = characteristic.getValue();
        //            if (data != null && data.length > 0) {
        //                final StringBuilder stringBuilder = new StringBuilder(data.length);
        //                for(byte byteChar : data)
        //                    stringBuilder.append(String.format("%02X ", byteChar));
        //                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        //            }
        //            sendBroadcast(intent);
        //        }

    }

    inner class LocalBinder : Binder() {
        internal val service: BluetoothLeService
            get() = this@BluetoothLeService
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    init {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
            }
        }

        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
        }

    }
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                return true
            } else {
                return false
            }
        }

        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic,
                                      enabled: Boolean) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
            val descriptor = characteristic.getDescriptor(
                    UUID.fromString(characteristic.uuid.toString()))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }

    private fun sendHeartRateToArtikCloud() {
        Log.i("GHJ", "sendhertrate to arktik cloud")
//        ArtikCloudSession.getInstance().onNewHeartRate(mHeartRateForArtikCloud, System.currentTimeMillis())

    }

    companion object {
        private val TAG = BluetoothLeService::class.java.simpleName

        private val STATE_DISCONNECTED = 0
        private val STATE_CONNECTING = 1
        private val STATE_CONNECTED = 2

        val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

        val UUID_HEART_RATE_MEASUREMENT = UUID.fromString(UUID.randomUUID().toString())
    }


}

//package com.mathieu.sensorme
//
//import android.app.ProgressDialog
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothServerSocket
//import android.bluetooth.BluetoothSocket
//import android.content.Context
//import android.util.Log
//
//import java.io.IOException
//import java.io.InputStream
//import java.io.OutputStream
//import java.nio.charset.Charset
//import java.util.UUID
//
///**
// * Created by User on 12/21/2016.
// */
//
//class BluetoothConnectionService(internal var mContext: Context) {
//
//    private val mBluetoothAdapter: BluetoothAdapter
//
//    private var mInsecureAcceptThread: AcceptThread? = null
//
//    private var mConnectThread: ConnectThread? = null
//    private var mmDevice: BluetoothDevice? = null
//    private var deviceUUID: UUID? = null
//    internal var mProgressDialog: ProgressDialog? = null
//
//    private var mConnectedThread: ConnectedThread? = null
//
//    init {
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        start()
//    }
//
//
//    /**
//     * This thread runs while listening for incoming connections. It behaves
//     * like a server-side client. It runs until a connection is accepted
//     * (or until cancelled).
//     */
//    private inner class AcceptThread : Thread() {
//
//        // The local server socket
//        private val mmServerSocket: BluetoothServerSocket?
//
//        init {
//            var tmp: BluetoothServerSocket? = null
//
//            // Create a new listening server socket
//            try {
//                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE)
//
//                Log.d(TAG, "AcceptThread: Setting up Server using: $MY_UUID_INSECURE")
//            } catch (e: IOException) {
//                Log.e(TAG, "AcceptThread: IOException: " + e.message)
//            }
//
//            mmServerSocket = tmp
//        }
//
//        override fun run() {
//            Log.d(TAG, "run: AcceptThread Running.")
//
//            var socket: BluetoothSocket? = null
//
//            try {
//                // This is a blocking call and will only return on a
//                // successful connection or an exception
//                Log.d(TAG, "run: RFCOM server socket start.....")
//
//                socket = mmServerSocket!!.accept()
//
//                Log.d(TAG, "run: RFCOM server socket accepted connection.")
//
//            } catch (e: IOException) {
//                Log.e(TAG, "AcceptThread: IOException: " + e.message)
//            }
//
//            //talk about this is in the 3rd
//            if (socket != null) {
//                connected(socket, mmDevice)
//            }
//
//            Log.i(TAG, "END mAcceptThread ")
//        }
//
//        fun cancel() {
//            Log.d(TAG, "cancel: Canceling AcceptThread.")
//            try {
//                mmServerSocket!!.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.message)
//            }
//
//        }
//
//    }
//
//    /**
//     * This thread runs while attempting to make an outgoing connection
//     * with a device. It runs straight through; the connection either
//     * succeeds or fails.
//     */
//    private inner class ConnectThread(device: BluetoothDevice, uuid: UUID) : Thread() {
//        private var mmSocket: BluetoothSocket? = null
//
//        init {
//            Log.d(TAG, "ConnectThread: started.")
//            mmDevice = device
//            deviceUUID = uuid
//        }
//
//        override fun run() {
//            var tmp: BluetoothSocket? = null
//            Log.i(TAG, "RUN mConnectThread ")
//
//            // Get a BluetoothSocket for a connection with the
//            // given BluetoothDevice
//            try {
//                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: $MY_UUID_INSECURE")
//                tmp = mmDevice!!.createRfcommSocketToServiceRecord(deviceUUID)
//            } catch (e: IOException) {
//                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.message)
//            }
//
//            mmSocket = tmp
//
//            // Always cancel discovery because it will slow down a connection
//            mBluetoothAdapter.cancelDiscovery()
//
//            // Make a connection to the BluetoothSocket
//
//            try {
//                // This is a blocking call and will only return on a
//                // successful connection or an exception
//                mmSocket!!.connect()
//
//                Log.d(TAG, "run: ConnectThread connected.")
//            } catch (e: IOException) {
//                // Close the socket
//                try {
//                    mmSocket!!.close()
//                    Log.d(TAG, "run: Closed Socket.")
//                } catch (e1: IOException) {
//                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.message)
//                }
//
//                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: $MY_UUID_INSECURE")
//            }
//
//            //will talk about this in the 3rd video
//
//            mmSocket?.let { connected(it, mmDevice) }
//        }
//
//        fun cancel() {
//            try {
//                Log.d(TAG, "cancel: Closing Client Socket.")
//                mmSocket!!.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.message)
//            }
//
//        }
//    }
//
//
//    /**
//     * Start the chat service. Specifically start AcceptThread to begin a
//     * session in listening (server) mode. Called by the Activity onResume()
//     */
//    @Synchronized
//    fun start() {
//        Log.d(TAG, "start")
//
//        // Cancel any thread attempting to make a connection
//        if (mConnectThread != null) {
//            mConnectThread!!.cancel()
//            mConnectThread = null
//        }
//        if (mInsecureAcceptThread == null) {
//            mInsecureAcceptThread = AcceptThread()
//            mInsecureAcceptThread!!.start()
//        }
//    }
//
//    /**
//     *
//     * AcceptThread starts and sits waiting for a connection.
//     * Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
//     */
//
//    fun startClient(device: BluetoothDevice, uuid: UUID) {
//        Log.d(TAG, "startClient: Started.")
//
//        //initprogress dialog
//        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...", true)
//
//        mConnectThread = ConnectThread(device, uuid)
//        mConnectThread!!.start()
//    }
//
//    /**
//     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
//     * receiving incoming data through input/output streams respectively.
//     */
//    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
//        private val mmInStream: InputStream?
//        private val mmOutStream: OutputStream?
//
//        init {
//            Log.d(TAG, "ConnectedThread: Starting.")
//            var tmpIn: InputStream? = null
//            var tmpOut: OutputStream? = null
//
//            //dismiss the progressdialog when connection is established
//            try {
//                mProgressDialog?.dismiss()
//            } catch (e: NullPointerException) {
//                e.printStackTrace()
//            }
//
//
//            try {
//                tmpIn = mmSocket.inputStream
//                tmpOut = mmSocket.outputStream
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//            mmInStream = tmpIn
//            mmOutStream = tmpOut
//        }
//
//        override fun run() {
//            val buffer = ByteArray(1024)  // buffer store for the stream
//
//            var bytes: Int // bytes returned from read()
//
//            // Keep listening to the InputStream until an exception occurs
//            while (true) {
//                // Read from the InputStream
//                try {
//                    bytes = mmInStream!!.read(buffer)
//                    val incomingMessage = String(buffer, 0, bytes)
//                    Log.d(TAG, "InputStream: $incomingMessage")
//                } catch (e: IOException) {
//                    Log.e(TAG, "write: Error reading Input Stream. " + e.message)
//                    break
//                }
//
//            }
//        }
//
//        //Call this from the main activity to send data to the remote device
//        fun write(bytes: ByteArray) {
//            val text = String(bytes, Charset.defaultCharset())
//            Log.d(TAG, "write: Writing to outputstream: $text")
//            try {
//                mmOutStream!!.write(bytes)
//            } catch (e: IOException) {
//                Log.e(TAG, "write: Error writing to output stream. " + e.message)
//            }
//
//        }
//
//        /* Call this from the main activity to shutdown the connection */
//        fun cancel() {
//            try {
//                mmSocket.close()
//            } catch (e: IOException) {
//            }
//
//        }
//    }
//
//    private fun connected(mmSocket: BluetoothSocket, mmDevice: BluetoothDevice?) {
//        Log.d(TAG, "connected: Starting.")
//
//        // Start the thread to manage the connection and perform transmissions
//        mConnectedThread = ConnectedThread(mmSocket)
//        mConnectedThread!!.start()
//    }
//
//    /**
//     * Write to the ConnectedThread in an unsynchronized manner
//     *
//     * @param out The bytes to write
//     * @see ConnectedThread.write
//     */
//    fun write(out: ByteArray) {
//        // Create temporary object
//        val r: ConnectedThread
//
//        // Synchronize a copy of the ConnectedThread
//        Log.d(TAG, "write: Write Called.")
//        //perform the write
//        mConnectedThread!!.write(out)
//    }
//
//    companion object {
//        private val TAG = "BluetoothConnectionServ"
//
//        private val appName = "MYAPP"
//
//        private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
//    }
//
//}