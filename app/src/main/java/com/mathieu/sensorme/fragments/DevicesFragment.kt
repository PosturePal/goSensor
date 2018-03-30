package com.mathieu.sensorme.fragments

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_devices.*
import android.content.Intent
import android.content.IntentFilter
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
import com.mathieu.sensorme.BTDevice
import com.mathieu.sensorme.BluetoothConnectionService
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
    var mAvailableDevicesAdapter = DevicesListAdapter(this, ArrayList())
//
//    var mBluetoothConnection: BluetoothConnectionService = BluetoothConnectionService(this.context!!)

    var mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    var btdev: BluetoothDevice? = null

    private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")


    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "inside receiver")
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // discovery found a device
                    val d = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                    Log.i(TAG, "Found Dev name: ${d.name}")
                    mAvailableDevicesAdapter.addItem(d)
                    view?.findViewById<TextView>(R.id.stat_available_count)?.text = mAvailableDevicesAdapter.count.toString()
                    Log.i(TAG, "Added Dev name: ${d.name}")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    sync_devices?.alpha = 0.5f
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    sync_devices?.alpha = 0.9f

                }


            /**
             * Broadcast Receiver that detects bond state changes (Pairing status changes)
             */
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val mDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //3 cases:
                    //case1: bonded already
                    if (mDevice.bondState == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                        alert("I am here!")
                        var uds = mDevice.uuids
     //                   startConnection(mDevice)



//                        btdev = mDevice;
                    }
                    //case2: creating a bone
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                        alert("BondIING")
                        Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                    }
                    //case3: breaking a bond
                    if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                        alert("BOND NOOONE")

                        Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

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


        //create method for starting connection
    //***remember the conncction will fail and app will crash if you haven't paired first
    fun startConnection(btDevice: BluetoothDevice) {
//            btDevice.createBond()

//        var mBluetoothGatt = btDevice.connectGatt(this.context, false, mGattCallback)
        startBTConnection(btDevice, MY_UUID_INSECURE)
    }

    /**
     * starting chat service method
     */
    fun startBTConnection(device: BluetoothDevice, uuid: UUID) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.")

        val mBluetoothConnection = BluetoothConnectionService(this.context)
        mBluetoothConnection.startClient(device, uuid)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sync_devices -> {
//                Log.i(TAG, "Scanning...")
                if (!mBluetoothAdapter.isDiscovering) {
                    rescanBTDevices()
                    alert("Scan enabled..")
                }
//                if(sync_devices.alpha == 0.9f) {
//                    sync_devices.alpha = 0.5f
//                }
            }
            R.id.delete_devices -> {
                mBluetoothAdapter.cancelDiscovery()
                mAvailableDevicesAdapter.clear()
                stat_available_count.text = "0"
                alert("List is clean now")
            }
        }
    }

    private fun alert(txt: String) {
        Snackbar.make(view!!, txt, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
    }

    private fun rescanBTDevices() {

//        availableDevices.clear()
//        mAvailableDevicesAdapter.clear()
//        stat_available_count.text = "0"

        //    connectedDevices.clear()
        //    mConnectedDevicesAdapter.updateItems(connectedDevices)

        if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()
        mBluetoothAdapter.startDiscovery()


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
    }


    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, BluetoothAdapter.STATE_ON)
        }


        if (mBluetoothAdapter.isEnabled) {

            val filter: IntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            this.context.registerReceiver(mReceiver, filter)

            val filterFinish = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            this.context.registerReceiver(mReceiver, filterFinish)
//            connectedDevices.add(mBluetoothAdapter.bondedDevices.last())

            val filterBond = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            this.context.registerReceiver(mReceiver, filterBond)
            if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()

            mBluetoothAdapter.startDiscovery()
        }

        registered = true;



//        for (d in mBluetoothAdapter.bondedDevices) {
//            mAvailableDevicesAdapter.addItem(d)
//        }

        available_devices_list.adapter = mAvailableDevicesAdapter
        stat_available_count.text = mAvailableDevicesAdapter.count.toString()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()

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

        if (registered) {
            context.unregisterReceiver(mReceiver)
        }
    }


    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach();
    }

    public fun connect(address: String) {

        Log.i("Connection", "to ${address} now")

        val btDevice: BluetoothDevice = mBluetoothAdapter.getRemoteDevice(address)
//        btDevice.setPairingConfirmation(true)

        val btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                //        val btSocket = btDevice.createRfcommSocketToServiceRecord(btDevice.uuids[0].uuid)
        Log.i("TAG", "should i connect?")
        if(!btSocket.isConnected) {
            btSocket.connect()
            Log.i("TAG", "ttry to connect")

        }

        Log.i("TAG", "connection est")
        val input = btSocket.getInputStream()
        val dinput = DataInputStream(input)

        var b:ByteArray = ByteArray(256)
        dinput.readFully(b)

        alert(b.toString())

        Log.i("TAG", "closing bt")
        btSocket.close()


//        val bluetoothGatt:BluetoothGatt = device.connectGatt(this, false,  )
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