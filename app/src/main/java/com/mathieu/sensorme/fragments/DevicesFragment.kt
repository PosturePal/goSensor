package com.mathieu.sensorme.fragments

import android.app.AlertDialog
import android.bluetooth.*
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_devices.*
import android.support.design.widget.Snackbar
import android.widget.TextView
import com.mathieu.sensorme.DevicesListAdapter
import com.mathieu.sensorme.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_devices.view.*
import kotlinx.android.synthetic.main.nav_action.*
import android.bluetooth.BluetoothSocket
import android.os.Handler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import android.bluetooth.BluetoothDevice
import android.content.*
import android.location.LocationManager
import android.net.wifi.aware.SubscribeConfig
import android.view.ContextThemeWrapper
import com.mathieu.sensorme.BTDevice
import com.mathieu.sensorme.StageRenderGL
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import java.io.DataInputStream
import java.util.*


/**
 * TODO:https://www.youtube.com/watch?v=5e1Yh0fSZhQ
 *
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DevicesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DevicesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class DevicesFragment : Fragment(), View.OnClickListener {


    var registered: Boolean = false;
    public val title = "Devices"
    val TAG = "FragmentOne"
    var mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    public var mAvailableDevicesAdapter = DevicesListAdapter(this, ArrayList())

    // bluetooth

    public var rxBleClient: RxBleClient? = null
    var scanSubscription: Disposable? = null


    // opengl
    public var rendererStage:StageRenderGL? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        rxBleClient = RxBleClient.create(context)
        rendererStage = view?.devices_stage_render
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")

//        sync_devices.setOnClickListener(this)

        var inf = inflater!!.inflate(R.layout.fragment_devices, container, false)

        inf.sync_devices.setOnClickListener(this)
        inf.delete_devices.setOnClickListener(this)
//        inf.available_devices_list.isScrollContainer = false
//        activity.appbar.setVisibility(View.INVISIBLE);
        return inf

    }
// Bluetooth

    //
    //
    //


    fun rescanBTDevices() {
        scanSubscription?.dispose()
        scanSubscription = rxBleClient?.scanBleDevices(
                ScanSettings.Builder()
                        // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
                // add filters if needed
        )!!.subscribe(
                { scanResult ->
                    Log.i(TAG, "ok, " + scanResult.toString())
                    mAvailableDevicesAdapter.addItem(scanResult.bleDevice.bluetoothDevice)
                }
                , { e -> Log.i(TAG, "error!" + e.toString()) }
                , { Log.i(TAG, "Completed") }
        );
    }

    //


    public fun cancelScan() {
        scanSubscription?.dispose()
    }


    fun clearDevices() {
        mAvailableDevicesAdapter.clear()
    }

    fun isScanning(): Boolean {
        return scanSubscription != null && scanSubscription!!.isDisposed
    }

    fun connect(macAddress:String){

    }

    //
    // =============== BT end =============
    //

    //create method for starting connection
    //***remember the conncction will fail and app will crash if you haven't paired first
    fun startConnection(btDevice: BluetoothDevice) {
//            btDevice.createBond()

//        var mBluetoothGatt = btDevice.connectGatt(this.context, false, mGattCallback)
//        startBTConnection(btDevice, MY_UUID_INSECURE)
    }

    /**
     * starting chat service method
     */
    fun startBTConnection(device: BluetoothDevice, uuid: UUID) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.")
//        mBluetoothConnection.connect(uuid.toString())
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sync_devices -> {
//                Log.i(TAG, "Scanning...")
                if (sync_devices.alpha == 0.9f) { //&& !isScanning()) {
                    // start scan
                    rescanBTDevices()
                    sync_devices.alpha = 0.5f
                    alert("Scan enabled..")
                } else {
                    cancelScan()
                    sync_devices.alpha = 0.9f
                }
            }
            R.id.delete_devices -> {
                cancelScan()
                clearDevices()
                stat_available_count.text = "0"
                alert("List is clean now")
            }
        }
    }

    public fun alert(txt: String) {
        view?.let {
            Snackbar.make(it, txt, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
    }


    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    private fun buildAlertMessageNoGps() {
        val ctw = ContextThemeWrapper(this.context, R.style.Theme_AppCompat_Light_Dialog_Alert)

        val builder = AlertDialog.Builder(ctw)
        builder.setMessage("Your GPS seems to be disabled, it's required for effective Bluetooth communication between BLE device and your phone")
            .setCancelable(false)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                })
            .setNegativeButton("No", DialogInterface.OnClickListener{ dialog, id ->
                    dialog.cancel();
            });
        val alert = builder.create();
        alert.show();
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        val manager = this.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, BluetoothAdapter.STATE_ON)

        }


        if (mBluetoothAdapter.isEnabled) {


//
//            val filter: IntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//            this.context.registerReceiver(mReceiver, filter)
//
//            val filterFinish = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//            this.context.registerReceiver(mReceiver, filterFinish)
////            connectedDevices.add(mBluetoothAdapter.bondedDevices.last())
//
//            val filterBond = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
//            this.context.registerReceiver(mReceiver, filterBond)
//            if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()
//
//            mBluetoothAdapter.startDiscovery()
        }

//        registered = true;


//        for (d in mBluetoothAdapter.bondedDevices) {
//            mAvailableDevicesAdapter.addItem(d)
//        }

        available_devices_list.adapter = mAvailableDevicesAdapter
        stat_available_count.text = mAvailableDevicesAdapter.count.toString()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()

        mAvailableDevicesAdapter.destroy()
        cancelScan()
//        activity.appbar.setVisibility(View.VISIBLE);
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()


        cancelScan()
        mAvailableDevicesAdapter.destroy()
//        mBluetoothConnection.close()
//        if (registered) {
//            context.unregisterReceiver(mReceiver)
//        }
    }


    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach();
    }
}

/*
class MyBluetoothService {
    private val mHandler: Handler? = null // handler that gets info from Bluetooth service

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        companion object {
            val MESSAGE_READ = 0
            val MESSAGE_WRITE = 1
            val MESSAGE_TOAST = 2
        }

        // ... (Add other message types here as needed.)
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        private var mmBuffer: ByteArray? = null // mmBuffer store for the stream

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = mmSocket.inputStream
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when creating input stream", e)
            }

            try {
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when creating output stream", e)
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            mmBuffer = ByteArray(1024)
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream!!.read(mmBuffer)
                    // Send the obtained bytes to the UI activity.
                    val readMsg = mHandler!!.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer)
                    readMsg.sendToTarget()
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream!!.write(bytes)

                // Share the sent message with the UI activity.
                val writtenMsg = mHandler!!.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer)
                writtenMsg.sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = mHandler!!.obtainMessage(MessageConstants.MESSAGE_TOAST)
                val bundle = Bundle()
                bundle.putString("toast",
                        "Couldn't send data to the other device")
                writeErrorMsg.setData(bundle)
                mHandler.sendMessage(writeErrorMsg)
            }

        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }

        }
    }

    companion object {
        private val TAG = "MY_APP_DEBUG_TAG"
    }
}
*/