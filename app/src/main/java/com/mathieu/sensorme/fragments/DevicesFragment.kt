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


    var mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


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
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    sync_devices?.alpha = 0.5f
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sync_devices -> {
//                Log.i(TAG, "Scanning...")
                if(sync_devices.alpha == 0.5f) {
                    rescanBTDevices()
                    alert(v, "Scan enabled..")
                    sync_devices.alpha = 0.9f
                }
            }
            R.id.delete_devices -> {
                mBluetoothAdapter.cancelDiscovery()
                mAvailableDevicesAdapter.clear()
                stat_available_count.text = "0"
                alert(v, "List is clean now")
            }
        }
    }

    private fun alert(v: View, txt: String) {

        Snackbar.make(v, txt, Snackbar.LENGTH_SHORT)
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
            if (mBluetoothAdapter.isDiscovering) mBluetoothAdapter.cancelDiscovery()

            mBluetoothAdapter.startDiscovery()
        }

        registered = true;


        for (d in mBluetoothAdapter.bondedDevices) {
            mAvailableDevicesAdapter.addItem(d)
        }

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

}