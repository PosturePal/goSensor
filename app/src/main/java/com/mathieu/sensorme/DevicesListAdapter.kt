package com.mathieu.sensorme

import android.bluetooth.BluetoothDevice
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.mathieu.sensorme.fragments.DevicesFragment

class DevicesListAdapter(private var deviceFr:DevicesFragment, private var items: ArrayList<BluetoothDevice>) : BaseAdapter(){
    private class ViewHolder(row: View?){
        var deviceName:TextView? = null
        var deviceStatus:TextView? = null
        var deviceAddress:String = ""
        init {
            this.deviceName = row?.findViewById(R.id.device_item_name)
            this.deviceStatus = row?.findViewById(R.id.device_item_status)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view:View?
        val viewHolder: ViewHolder


        if(convertView == null) {
            view = deviceFr.layoutInflater.inflate(R.layout.device_item, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        }
        else
        {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val device = items[position]
        viewHolder.deviceName?.text = device.name
        viewHolder.deviceStatus?.text =
                when(device.bondState){
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

    public fun updateItems(list:ArrayList<BluetoothDevice>)
    {
        items = list
        notifyDataSetChanged()
    }
    public fun addItem(btd:BluetoothDevice)
    {
        if(!items.contains(btd)) {
            items.add(btd)
            notifyDataSetChanged()
        }
    }
    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }
}