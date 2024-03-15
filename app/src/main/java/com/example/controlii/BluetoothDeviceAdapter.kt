package com.example.controlii


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.controlii.MyStoreInfo.Companion.ACTIVE
import com.example.controlii.MyStoreInfo.Companion.CONNECTED_STATUS
import com.example.controlii.MyStoreInfo.Companion.CONNECTING_
import com.example.controlii.MyStoreInfo.Companion.CONNECTING_STATUS_
import com.example.controlii.MyStoreInfo.Companion.CONNECTION_FAILED_STATUS
import com.example.controlii.MyStoreInfo.Companion.CONNECTION_STATUS_KEY
import com.example.controlii.MyStoreInfo.Companion.DEFAULT_STATUS
import com.example.controlii.MyStoreInfo.Companion.DISCONNECTED_STATUS
import com.example.controlii.MyStoreInfo.Companion.NO_POSITION
import com.example.controlii.MyStoreInfo.Companion.PRESERVE_ITEM_POSITION
import com.example.controlii.MyStoreInfo.Companion.SELECTED_ITEM_POSITION
import kotlin.properties.Delegates

class BluetoothDeviceAdapter
    (
    private val devicesList: ArrayList<DeviceInfoModel>,
    val ccontext: Context,
    val  bluetoothDeviceInterface: BluetoothDeviceInterface,
    val requestBluetoothTurnOn: RequestBluetoothTurnOn
     ):
    RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceFragmentHolder> (){

    var selectedItemPosition = RecyclerView.NO_POSITION

    lateinit var myStoreInfo: MyStoreInfo



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceFragmentHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.device_fragment,
                parent,
                false
            )
        return  DeviceFragmentHolder(itemView)
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    @SuppressLint("NotifyDataSetChanged", "MissingPermission")
    override fun onBindViewHolder(holder: DeviceFragmentHolder, position: Int) {


        val currentDevice: DeviceInfoModel = devicesList.get(position)

        val name = currentDevice.getName
        holder.deviceName.text = name

        val device = currentDevice.bluetoothDevice


        when(device.bluetoothClass.deviceClass) {
            BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE,
            BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES,
            BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET->{
                holder.classificationIcon.setImageDrawable(ContextCompat.getDrawable(ccontext, R.drawable.device_audio_video))
            }

            BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> {
                holder.classificationIcon.setImageDrawable(ContextCompat.getDrawable(ccontext, R.drawable.device_audio))
            }

            BluetoothClass.Device.PHONE_CELLULAR,
            BluetoothClass.Device.PHONE_SMART -> {
                holder.classificationIcon.setImageDrawable(ContextCompat.getDrawable(ccontext, R.drawable.device_phone))
            }
        }
        when(device.bluetoothClass.majorDeviceClass) {
            BluetoothClass.Device.Major.COMPUTER -> {
                holder.classificationIcon.setImageDrawable(ContextCompat.getDrawable(ccontext, R.drawable.device_computer))
            }
        }



        myStoreInfo = MyStoreInfo(ccontext)

        val isMatchPosition
        = position == myStoreInfo.readInt(SELECTED_ITEM_POSITION, NO_POSITION)!! &&
                myStoreInfo.readInt(SELECTED_ITEM_POSITION, NO_POSITION)!! != NO_POSITION


        if(isMatchPosition) {

            val CONNECTED
                    = myStoreInfo.readInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS) == CONNECTED_STATUS
            val DISCONNECTED
                    = myStoreInfo.readInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS) == DISCONNECTED_STATUS
            val CONNECTION_FAILED
                    = myStoreInfo.readInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS) == CONNECTION_FAILED_STATUS
            val CONNECTING
                    = myStoreInfo.readInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS) == CONNECTING_STATUS_
            val DEFAULT
                    = myStoreInfo.readInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS) == DEFAULT_STATUS




            when {
                CONNECTED -> {
                    Log.i("CONNECTION_STATUS", "CONNECTED ${device.name}")
                    holder.apply {
                        //foreground
                        deviceActive.apply {
                            visibility = View.VISIBLE
                            text = ACTIVE
                            setTextColor(ContextCompat.getColor(ccontext, R.color.button_text))
                        }
                        iconConnect.apply {
                            drawable.setTint(ContextCompat.getColor(ccontext, R.color.button_text))
                            visibility = View.VISIBLE
                        }

                        classificationIcon.drawable.setTint(ContextCompat.getColor(ccontext, R.color.button_text))
                        deviceName.setTextColor(ContextCompat.getColor(ccontext, R.color.button_text))

                        //background
                        viewContainer.setBackgroundColor(ContextCompat.getColor(ccontext, R.color.color_primary))
                    }

                }
                CONNECTING -> {
                    Log.i("CONNECTION_STATUS", "CONNECTING ${device.name}")
                    holder.apply {
                        //foreground
                        deviceActive.apply {
                            visibility = View.VISIBLE
                            text = CONNECTING_
                            setTextColor(ContextCompat.getColor(ccontext, R.color.text_color))
                        }
                        iconConnect.apply {
                            drawable.setTint(ContextCompat.getColor(ccontext, R.color.text_color))
                            visibility = View.VISIBLE
                        }

                        classificationIcon.drawable.setTint(ContextCompat.getColor(ccontext, R.color.text_color))
                        deviceName.setTextColor(ContextCompat.getColor(ccontext, R.color.text_color))

                        //background
                        viewContainer.setBackgroundColor(ContextCompat.getColor(ccontext, R.color.container))
                    }
                }
                DISCONNECTED || CONNECTION_FAILED || DEFAULT -> {

                    holder.apply {
                        //foreground
                        deviceActive.apply {
                            visibility = View.GONE
                            text = CONNECTING_
                            setTextColor(ContextCompat.getColor(ccontext, R.color.text_color))
                        }

                        iconConnect.apply {
                            drawable.setTint(ContextCompat.getColor(ccontext, R.color.text_color))
                            visibility = View.GONE
                        }

                        classificationIcon.drawable.setTint(ContextCompat.getColor(ccontext, R.color.text_color))
                        deviceName.setTextColor(ContextCompat.getColor(ccontext, R.color.text_color))

                        //background
                        viewContainer.setBackgroundColor(ContextCompat.getColor(ccontext, R.color.transparent))
                    }
                }

            }
            when{
                DISCONNECTED -> Log.i("CONNECTION_STATUS", "DISCONNECTED ${device.name}")
                CONNECTION_FAILED -> Log.i("CONNECTION_STATUS", "FAILED ${device.name}")
                DEFAULT -> Log.i("CONNECTION_STATUS", "DEFAULT ${device.name}")
            }

        }
        else{
            holder.apply {
                //foreground
                deviceActive.apply {
                    visibility = View.GONE
                    text = CONNECTING_
                    setTextColor(ContextCompat.getColor(ccontext, R.color.text_color))
                }
                iconConnect.apply {
                    drawable.setTint(ContextCompat.getColor(ccontext, R.color.text_color))
                    visibility = View.GONE
                }
                classificationIcon.drawable.setTint(ContextCompat.getColor(ccontext, R.color.text_color))
                deviceName.setTextColor(ContextCompat.getColor(ccontext, R.color.text_color))

                //background
                viewContainer.setBackgroundColor(ContextCompat.getColor(ccontext, R.color.transparent))
            }
        }



    }


    @SuppressLint("ClickableViewAccessibility")
    inner class DeviceFragmentHolder(deviceView: View): RecyclerView.ViewHolder(deviceView){
        var deviceName: TextView = deviceView.findViewById(R.id.deviceName)
        var deviceActive: TextView = deviceView.findViewById(R.id.deviceConnected)
        var viewContainer: LinearLayout = deviceView.findViewById(R.id.viewContainer)
        var iconConnect: ImageView = deviceView.findViewById(R.id.iconConnect)
        var classificationIcon: ImageView =deviceView.findViewById(R.id.classificationIcon)



        init {
            myStoreInfo = MyStoreInfo(ccontext)
            val bluetoothAdapter =
                (ccontext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

            deviceView.setOnClickListener {

                if( bluetoothAdapter.isEnabled) {
                    if(myStoreInfo.readInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS) != CONNECTED_STATUS
                        &&myStoreInfo.readInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS) != CONNECTING_STATUS_) {
                        val previousSelectedPos = selectedItemPosition
                        selectedItemPosition = adapterPosition

                        if (previousSelectedPos != RecyclerView.NO_POSITION) {
                            notifyItemChanged(previousSelectedPos)
                        }
                        val itemAtPreviousPos = devicesList[selectedItemPosition]


                        bluetoothDeviceInterface.onClickBluetoothConnectListener(
                            itemAtPreviousPos.getName.toString(),
                            itemAtPreviousPos.getAddress.toString()
                        )


                        myStoreInfo.writeInt(SELECTED_ITEM_POSITION , adapterPosition)
                        myStoreInfo.writeInt(PRESERVE_ITEM_POSITION, adapterPosition)
                        notifyItemChanged(selectedItemPosition)
                    }
                }
                else {
                    requestBluetoothTurnOn.callBluetoothTurnOnRequest(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }


            }
            iconConnect.setOnClickListener{
                if (myStoreInfo.readInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS) == CONNECTED_STATUS) {
                    bluetoothDeviceInterface.onClickBluetoothDisconnectListener()
                    myStoreInfo.writeInt(SELECTED_ITEM_POSITION, adapterPosition)
                }
                if (myStoreInfo.readInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS) == CONNECTING_STATUS_) {
                    bluetoothDeviceInterface.onClickBluetoothCancelListener()
                    myStoreInfo.writeInt(SELECTED_ITEM_POSITION, adapterPosition)
                }
            }


        }
    }

    fun myNotifyItemChanged(position: Int) {
        notifyItemChanged(position)
    }




}

interface BluetoothDeviceInterface {
    fun onClickBluetoothConnectListener(name: String, address: String)
    fun onClickBluetoothDisconnectListener()
    fun onClickBluetoothCancelListener()
}



interface RequestBluetoothTurnOn {
    fun callBluetoothTurnOnRequest(intent: Intent)
}



