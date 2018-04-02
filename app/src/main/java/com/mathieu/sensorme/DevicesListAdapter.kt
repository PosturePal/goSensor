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
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_GYROSCOPE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.aware.Characteristics
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.Timeout
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_devices.*
import kotlinx.android.synthetic.main.fragment_devices.view.*
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.collections.ArrayList
import kotlin.concurrent.timer
import kotlin.experimental.and


class DevicesListAdapter(private var deviceFr: DevicesFragment, private var items: ArrayList<BluetoothDevice>) : BaseAdapter() {
    private class ViewHolder(private val deviceFr: DevicesFragment, row: View?) {
        var deviceAddress: String = ""
        var device: BluetoothDevice? = null
        private var connected: Boolean = false


        var mConnectionDisposable: Disposable? = null
        var mRxDevice: RxBleDevice? = null

        var mRxBleConnection: RxBleConnection? = null

        val TAG = "ViewHolder List"

        var connectBtn: Button? = null
        var readBtn: Button? = null
        var deviceName: TextView? = null
        var deviceStatus: TextView? = null

        val accGyroCharacteristicUUID: UUID = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb")

//        var madgwickAHRS = MadgwickAHRS(0.01f, 0.041f)

        var madgwickAHRS = MadgwickAHRS(500.0f, 0.1f)
        private val madgwickTimer = Timer()
        private var lastUpdate: Long = 0
        var lpPitch: Float = 0.0f
        var lpRoll: Float = 0.0f
        var lpYaw: Float = 0.0f


        var calibIterator:Int = 0
        var calibOffsets = FloatArray(6, { i -> 0.0f})
        init {
            this.deviceName = row?.findViewById(R.id.device_item_name)
            this.deviceStatus = row?.findViewById(R.id.device_item_status)
            this.connectBtn = row?.findViewById<Button>(R.id.device_connect_button)
            this.readBtn = row?.findViewById<Button>(R.id.device_read_button)

            connectBtn?.setOnClickListener { view ->

                if (mRxDevice != null && mRxDevice?.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED) {
                    // connected, disconnect
                    mConnectionDisposable?.dispose()
                    mConnectionDisposable = null
                    mRxBleConnection = null
                    mRxDevice = null
                    deviceFr.alert("Disconnected device with address " + this.deviceAddress)
                    connectBtn?.alpha = 1.0f
                } else {
                    startConnection()
                }
            }

            readBtn?.setOnClickListener { view ->

                Thread {
                    while (true) {
                        read()
                    }
                }.start()
            }



//            mSensorManager = deviceFr.context.getSystemService(SENSOR_SERVICE) as SensorManager?
//            mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//            mGyroscope = mSensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
//            madgwickTimer.scheduleAtFixedRate(DoMadgwick(madgwickAHRS),
//                    1000, 10)
//
//            mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//            mSensorManager!!.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);


//            var x =

        }


//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//            Log.i(TAG, "onaccuracy changed")
//            ///TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//        override fun onSensorChanged(event: SensorEvent?) {
//            Log.i(TAG, "onSchngd")
//            if(readFromAndroid)
//            {
//                if (event?.sensor?.getType() == Sensor.TYPE_ACCELEROMETER) {
//                    aax = event.values[0];
//                    aay = event.values[1];
//                    aaz = event.values[2];
//                    Log.i(TAG, "OnSensorChanged: x: " + aax.toString()
//                            + ", y: " + aay.toString()
//                            + ", z: " + aaz.toString())
//                } else if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
//                    agx = event.values[0];
//                    agy = event.values[1];
//                    agz = event.values[2];
//                    Log.i(TAG, "OnSensorChanged: x: " + agx.toString() + ", y: " + agy.toString() + ", z: " + agz.toString())
//                }
//
//
//                val now: Long = System.currentTimeMillis()
//                madgwickAHRS.SamplePeriod = (now - lastUpdate) / 1000.0f //timestamp.toFloat()
//                lastUpdate = now
//
//                madgwickAHRS.Update(agx.toFloat(), agy.toFloat(), agz.toFloat(),
//                        aax.toFloat(), aay.toFloat(), aaz.toFloat())
//
//                lpPitch = (lpPitch * 0.2 + madgwickAHRS.MadgPitch * 0.8).toFloat()
//                lpRoll = (lpRoll * 0.2 + madgwickAHRS.MadgRoll * 0.8).toFloat()
//                lpYaw = (lpYaw * 0.2 + madgwickAHRS.MadgYaw * 0.8).toFloat()
//
//                Log.i("Android:", "pitch: " + lpPitch.toString()
//                        + "roll: " + lpRoll.toString()
//                        + "yaw: " + lpYaw.toString())
//
//
//                deviceFr.view!!.devices_stage_render.mStageRenderer.setRotation(lpRoll, lpPitch, lpYaw)
//            }
//        }

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


        fun read() {
            if (device == null) {
                deviceFr.alert("There is no device for connection")
                return
            }

            if (mRxDevice?.connectionState != RxBleConnection.RxBleConnectionState.CONNECTED) {
                deviceFr.alert("device is not connected")
                return
            }
            mRxBleConnection?.readCharacteristic(accGyroCharacteristicUUID)?.subscribe(
                    { characteristicValue ->
                        if(!deviceFr.readFromAndroid) {

                            //                        Log.i(TAG,"read char: " + toHex(characteristicValue) + ", bytes are\n " + characteristicValue.contentToString())
                            val bytes = characteristicValue.asList()

                            if (bytes.size > 16) {
                                // 44020100e7fffdfff9039d00fbee9cfd0100d022
                                /*
                          1    2    3  4    5    6   7    8   9   10  11   12   13   14   15   16   17 18 19   20
                        [-40, -114, 0, 0,| -24, -1,| -6, -1,| -7, 3,| 122, 0|, -40, -18|, -13, -3,| 1, 0, -48, 34]
                         -- timestamp -- | - ax -  |- ay -  | - az -| - gx -| - gy -   | - gz -   | -- reserve --
                         */
                                // TODO: here may be an error
                                // TODO: maybe use C for converting bytes to uints?
                                var timestamp = ByteBuffer.allocate(4).put(bytes.subList(0, 4).toByteArray()).getInt(0)
                                var ax = ByteBuffer.allocate(2).put(bytes.subList(4, 6).toByteArray()).getShort(0).toFloat()/1000.0f
                                var ay = ByteBuffer.allocate(2).put(bytes.subList(6, 8).toByteArray()).getShort(0).toFloat()/1000.0f
                                var az = ByteBuffer.allocate(2).put(bytes.subList(8, 10).toByteArray()).getShort(0).toFloat()/1000.0f
                                var gx = ByteBuffer.allocate(2).put(bytes.subList(10, 12).toByteArray()).getShort(0).toFloat()/1000.0f
                                var gy = ByteBuffer.allocate(2).put(bytes.subList(12, 14).toByteArray()).getShort(0).toFloat()/1000.0f
                                var gz = ByteBuffer.allocate(2).put(bytes.subList(14, 16).toByteArray()).getShort(0).toFloat()/1000.0f

                                // reserved - (17, 21)
                                Log.i("I:", "data from sensors: "
                                        + "; ax " + ax.toString()
                                        + "; ay " + ay.toString()
                                        + "; az " + az.toString()
                                        + "; gx " + gx.toString()
                                        + "; gy " + gy.toString()
                                        + "; gz " + gz.toString()
                                )

                                if(calibIterator < 32)
                                {
                                    Log.i(TAG, "Calib?" + calibIterator)
                                    calibOffsets[0] = calibOffsets[0] + gx
                                    calibOffsets[1] = calibOffsets[1] + gy
                                    calibOffsets[2] = calibOffsets[2] + gz

                                    calibOffsets[3] = calibOffsets[3] + ax
                                    calibOffsets[4] = calibOffsets[4] + ay
                                    calibOffsets[5] = calibOffsets[5] + az

                                    calibIterator++;
                                }
                                else {
                                    val now: Long = System.currentTimeMillis()
                                    madgwickAHRS.SamplePeriod = (now - lastUpdate) / 1000.0f //timestamp.toFloat()
                                    lastUpdate = now

                                    // calib
                                    gx -= calibOffsets[0]/32
                                    gy -= calibOffsets[1]/32
                                    gz -= calibOffsets[2]/32


                                    ax -= calibOffsets[3]/32
                                    ay -= calibOffsets[4]/32
                                    az -= calibOffsets[5]/32


                                    madgwickAHRS.Update(gx.toFloat(),
                                            gy.toFloat(),
                                            gz.toFloat(),
                                            ax.toFloat(),
                                            ay.toFloat(),
                                            az.toFloat())

                                    lpPitch = (lpPitch * 0.2 + madgwickAHRS.MadgPitch * 0.8).toFloat()
                                    lpRoll = (lpRoll * 0.2 + madgwickAHRS.MadgRoll * 0.8).toFloat()
                                    lpYaw = (lpYaw * 0.2 + madgwickAHRS.MadgYaw * 0.8).toFloat()

                                    Log.i("Android:", "pitch: " + lpPitch.toString()
                                            + "roll: " + lpRoll.toString()
                                            + "yaw: " + lpYaw.toString())


                                    deviceFr.view!!.devices_stage_render.mStageRenderer.setRotation(lpRoll, lpPitch, lpYaw)
                                }
                            }
                        }
//                        Log.i(TAG, ByteBuffer.wrap(timestamp.toByteArray()).int.toString())
                    },
                    { e ->
                        Log.i(TAG, "error reading char" + e.toString())
                    }

            )

            fun fromByteArray(bytes: ByteArray): Int {
                return ByteBuffer.wrap(bytes).getInt();
            }
        }
//
//
//        class DoMadgwick(private val madgwickAHRS: MadgwickAHRS, private var lastUpdate:Long) : TimerTask() {
//            override fun run() {
//                val now: Long = System.currentTimeMillis()
//                madgwickAHRS.SamplePeriod = (now - lastUpdate) / 1000.0f;
//                lastUpdate = now;
//                madgwickAHRS.Update(gyro[0], gyro[1], gyro[2], accel[0], accel[1], accel[2], magnet[0], magnet[1], magnet[2]);
//                if (seriesAccx.size() > HISTORY_SIZE) {
//                    seriesAccx.removeFirst();
//                    seriesAccy.removeFirst();
//                    seriesAccz.removeFirst();
//                }
//
//                //add the latest history sample:
//                lpPitch = lpPitch * 0.2 + madgwickAHRS.MadgPitch * 0.8;
//                lpRpll = lpRpll * 0.2 + madgwickAHRS.MadgRoll * 0.8;
//                lpYaw = lpYaw * 0.2 + madgwickAHRS.MadgYaw * 0.8;
//                seriesAccx.addLast(null, lpPitch);
//                seriesAccy.addLast(null, lpRpll);
//                seriesAccz.addLast(null, lpYaw);
//                //  seriesAccx.addLast(null, gyro[0]);
//                //   seriesAccy.addLast(null, gyro[1]);
//                //  seriesAccz.addLast(null, gyro[2]);
////            plot.post(new Runnable() {
////                public void run() {
////            /* the desired UI update */
////                 //   labelaccx.setText(Double.toString(madgwickAHRS.MadgPitch));
////                  //  labelaccy.setText(Double.toString(madgwickAHRS.MadgRoll));
////                  //  labelaccz.setText(Double.toString(madgwickAHRS.MadgYaw));
////                   //labeldt.setText(Double.toString(madgwickAHRS.SamplePeriod));
////                    plot.redraw();
////                }
////            });
//            }
//
//        }

        fun startConnection() {
            if (device == null) {
                deviceFr.alert("There is no device for connection")
            }
            //device?.createBond()
            mRxDevice = deviceFr.rxBleClient!!.getBleDevice(deviceAddress);

            mConnectionDisposable?.dispose()
            mConnectionDisposable = mRxDevice!!.establishConnection(false) // <-- autoConnect flag
                    .subscribe(
                            { rxBleConnection ->
                                deviceFr.alert("YEAH, rxble connected")
                                deviceFr.alert("Connected device with address " + this.device?.address + " " + connected)
                                connectBtn?.alpha = 0.5f

                                mRxBleConnection = rxBleConnection
                            }
                            , { e ->
                        Log.i(TAG, "error!" + e.toString())
                        deviceFr.alert("Error while connecting device with address " + this.device?.address + " " + connected)
                        connectBtn?.alpha = 1.0f
                    }
                            , {
                        Log.i(TAG, "Completed")
                        deviceFr.alert("EWHATTATATATA FUCK")
                    }
                    );
        }

        public fun disconnect() {
            Log.i(TAG, "dsconnect proc")
//            mSensorManager?.unregisterListener(this);
//            if(mRxDevice != null && mRxDevice?.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED)
//            {
            // connected, disconnect
            mConnectionDisposable?.dispose()
            mConnectionDisposable = null
            mRxBleConnection = null
            mRxDevice = null
            Log.i(TAG, "disconnected frrom here")
//            }
        }
    }

    private var viewHolder: ViewHolder? = null

    public fun disconnect() {
        viewHolder?.disconnect()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View?

        if (convertView == null) {
            view = deviceFr.layoutInflater.inflate(R.layout.device_item, null)
            viewHolder = ViewHolder(deviceFr, view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }


        val device = items[position]
        viewHolder?.device = device
        viewHolder?.deviceName?.text = device.name
        viewHolder?.deviceStatus?.text = device.address
//                when (device.bondState) {
//                    BluetoothDevice.BOND_NONE -> "not paired"
//                    BluetoothDevice.BOND_BONDING -> "pairing"
//                    BluetoothDevice.BOND_BONDED -> "paired before"
//                    else -> device.address
//                }
        viewHolder?.deviceAddress = device.address
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
        disconnect()
        items.clear()
        notifyDataSetChanged()
    }
}