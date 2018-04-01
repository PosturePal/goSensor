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
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.Timeout
import io.reactivex.disposables.Disposable
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

        var madgwickAHRS = MadgwickAHRS(0.01f, 0.041f)
        private val madgwickTimer = Timer()
        private var lastUpdate: Long = 0
        var lpPitch = 0.0
        var lpRoll = 0.0
        var lpYaw = 0.0


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

//                Thread {
//                    while (true) {
                        read()
//                    }
//                }.start()
            }


//            madgwickTimer.scheduleAtFixedRate(DoMadgwick(madgwickAHRS),
//                    1000, 10)


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


        fun read() {
            if (device == null) {
                deviceFr.alert("There is no device for connection")
            }

            if (mRxDevice?.connectionState != RxBleConnection.RxBleConnectionState.CONNECTED) {
                deviceFr.alert("device is not connected")
                return;
            }
            mRxBleConnection?.readCharacteristic(accGyroCharacteristicUUID)?.subscribe(
                    { characteristicValue ->


                        //                        Log.i(TAG,"read char: " + toHex(characteristicValue) + ", bytes are\n " + characteristicValue.contentToString())
                        val bytes = characteristicValue.asList()

                        if (bytes.size > 16) {
                            // 44020100e7fffdfff9039d00fbee9cfd0100d022
                            /*
                          1    2    3  4    5    6   7    8   9   10  11   12   13   14   15   16   17 18 19   20
                        [-40, -114, 0, 0,| -24, -1,| -6, -1,| -7, 3,| 122, 0|, -40, -18|, -13, -3,| 1, 0, -48, 34]
                         -- timestamp -- | - ax -  |- ay -  | - az -| - gx -| - gy -   | - gz -   | -- reserve --
                         */

                            val timestamp = ByteBuffer.allocate(4).put(bytes.subList(0, 4).toByteArray()).getInt(0)
                            val ax = ByteBuffer.allocate(2).put(bytes.subList(4, 6).toByteArray()).getShort(0)
                            val ay = ByteBuffer.allocate(2).put(bytes.subList(6, 8).toByteArray()).getShort(0)
                            val az = ByteBuffer.allocate(2).put(bytes.subList(8, 10).toByteArray()).getShort(0)
                            val gx = ByteBuffer.allocate(2).put(bytes.subList(10, 12).toByteArray()).getShort(0)
                            val gy = ByteBuffer.allocate(2).put(bytes.subList(12, 14).toByteArray()).getShort(0)
                            val gz = ByteBuffer.allocate(2).put(bytes.subList(14, 16).toByteArray()).getShort(0)

                            // reserved - (17, 21)
                            Log.i(TAG, "Got : tmstmp "
                                    + timestamp.toString()
                                    + "; ax " + ax.toString()
                                    + "; ay " + ay.toString()
                                    + "; az " + az.toString()
                                    + "; gx " + gx.toString()
                                    + "; gy " + gy.toString()
                                    + "; gz " + gz.toString()
                            )

                            val now:Long = System.currentTimeMillis()
                            madgwickAHRS.SamplePeriod = (now - lastUpdate) / 1000.0f //timestamp.toFloat()
                            lastUpdate = now

                            madgwickAHRS.Update(gx.toFloat()/1000, gy.toFloat()/1000,gz.toFloat()/1000, ax.toFloat()/1000,ay.toFloat()/1000, az.toFloat()/1000)

                            lpPitch = lpPitch * 0.2 + madgwickAHRS.MadgPitch * 0.8;
                            lpRoll = lpRoll * 0.2 + madgwickAHRS.MadgRoll * 0.8;
                            lpYaw = lpYaw * 0.2 + madgwickAHRS.MadgYaw * 0.8;

                            Log.i("MAD DATA", "pitch: " + lpPitch.toString()
                            + "roll: " + lpRoll.toString()
                            + "yaw: " + lpYaw.toString())
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

    public fun destroy() {
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
        items.clear()
        notifyDataSetChanged()
    }
}