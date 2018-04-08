package com.mathieu.sensorme.fragments

import android.app.AlertDialog
import android.bluetooth.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_devices.*
import android.support.design.widget.Snackbar
import com.mathieu.sensorme.DevicesListAdapter
import com.mathieu.sensorme.R
import kotlinx.android.synthetic.main.fragment_devices.view.*
import android.bluetooth.BluetoothDevice
import android.content.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.view.ContextThemeWrapper
import com.mathieu.sensorme.MadgwickAHRS
import com.mathieu.sensorme.StageRenderGL
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable
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


class DevicesFragment : Fragment(), View.OnClickListener, SensorEventListener {


    var registered: Boolean = false;
    public val title = "Devices"
    val TAG = "FragmentOne"
    var mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    public var mAvailableDevicesAdapter = DevicesListAdapter(this, ArrayList())

    // bluetooth

    public var rxBleClient: RxBleClient? = null
    var scanSubscription: Disposable? = null


    // opengl
    public var rendererStage: StageRenderGL? = null


    var mSensorManager: SensorManager? = null
    var mAccelerometer: Sensor? = null
    var mGyroscope: Sensor? = null

    var agx = 0.0f
    var agy = 0.0f
    var agz = 0.0f
    var aax = 0.0f
    var aay = 0.0f
    var aaz = 0.0f


    public var readFromAndroid = true

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        rxBleClient = RxBleClient.create(context)
        rendererStage = StageRenderGL(context, null)
//        devices_stage_render.mStageRenderer.mCube = rendererStage

        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGyroscope = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "onaccuracy changed")
        ///TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var madgwickAHRS = MadgwickAHRS(500.0f, 0.1f)
    private val madgwickTimer = Timer()
    private var lastUpdate: Long = 0
    var lpPitch: Float = 0.0f
    var lpRoll: Float = 0.0f
    var lpYaw: Float = 0.0f


    override fun onSensorChanged(event: SensorEvent?) {
//        Log.i(TAG, "onSchngd")
        if (!readFromAndroid) {
            return
        }

        if (event?.sensor?.getType() == Sensor.TYPE_ACCELEROMETER) {
            aax = event.values[0];
            aay = event.values[1];
            aaz = event.values[2];
//            Log.i(TAG, "OnSensorChanged: x: " + aax.toString()
//                    + ", y: " + aay.toString()
//                    + ", z: " + aaz.toString())
        } else if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            agx = event.values[0];
            agy = event.values[1];
            agz = event.values[2];
//            Log.i(TAG, "OnSensorChanged: x: " + agx.toString() + ", y: " + agy.toString() + ", z: " + agz.toString())
        }


        Log.i("I:", "data from sensors: "
                + "; ax " + aax.toString()
                + "; ay " + aay.toString()
                + "; az " + aaz.toString()
                + "; gx " + agx.toString()
                + "; gy " + agy.toString()
                + "; gz " + agz.toString()
        )

        val now: Long = System.currentTimeMillis()
        madgwickAHRS.SamplePeriod = (now - lastUpdate) / 1000.0f //timestamp.toFloat()
        lastUpdate = now


        madgwickAHRS.Update(agx.toFloat(), agy.toFloat(), agz.toFloat(),
                aax.toFloat(), aay.toFloat(), aaz.toFloat())
//        lpPitch = (lpPitch * 0.2 + madgwickAHRS.MadgPitch * 0.8).toFloat()
//        lpRoll = (lpRoll * 0.2 + madgwickAHRS.MadgRoll * 0.8).toFloat()
//        lpYaw = (lpYaw * 0.2 + madgwickAHRS.MadgYaw * 0.8).toFloat()
//

        lpPitch = madgwickAHRS.MadgPitch.toFloat()
        lpRoll = madgwickAHRS.MadgRoll.toFloat()
        lpYaw = madgwickAHRS.MadgYaw.toFloat()
        Log.i("Android:", "pitch: " + lpPitch.toString()
                + "roll: " + lpRoll.toString()
                + "yaw: " + lpYaw.toString())


        view?.devices_stage_render?.mStageRenderer?.setRotation(-lpPitch, lpRoll, -lpYaw)

    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")

//        sync_devices.setOnClickListener(this)

        var inf = inflater!!.inflate(R.layout.fragment_devices, container, false)

        inf.sync_devices.setOnClickListener(this)
        inf.delete_devices.setOnClickListener(this)
        inf.android_imu_enable.setOnClickListener(this)
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
        mAvailableDevicesAdapter.disconnect()
        mAvailableDevicesAdapter.clear()
    }

    fun isScanning(): Boolean {
        return scanSubscription != null && scanSubscription!!.isDisposed
    }

    fun connect(macAddress: String) {

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
            R.id.android_imu_enable -> {
                readFromAndroid = !readFromAndroid
                if (readFromAndroid) {
                    android_imu_enable.alpha = 0.5f
                } else {
                    android_imu_enable.alpha = 0.9f
                }
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
                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
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


        mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager!!.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

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

        mAvailableDevicesAdapter.disconnect()
        mSensorManager?.unregisterListener(this)
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
        mAvailableDevicesAdapter.disconnect()
//        mBluetoothConnection.close()
//        if (registered) {
//            context.unregisterReceiver(mReceiver)
//        }
        mSensorManager?.unregisterListener(this)
    }


    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach();
    }
}