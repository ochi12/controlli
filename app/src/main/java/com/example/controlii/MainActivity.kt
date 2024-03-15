package com.example.controlii

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.UiModeManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.controlii.MyStoreInfo.Companion.CONNECTED_STATUS
import com.example.controlii.MyStoreInfo.Companion.CONNECTING_STATUS_
import com.example.controlii.MyStoreInfo.Companion.CONNECTION_FAILED_STATUS
import com.example.controlii.MyStoreInfo.Companion.CONNECTION_STATUS_KEY
import com.example.controlii.MyStoreInfo.Companion.DEFAULT_STATUS
import com.example.controlii.MyStoreInfo.Companion.DISCONNECTED_STATUS
import com.example.controlii.MyStoreInfo.Companion.NO_POSITION
import com.example.controlii.MyStoreInfo.Companion.SELECTED_ITEM_POSITION
import com.example.controlii.databinding.ActivityMainBinding
import com.example.controlii.databinding.ActivityMainStatusPillBinding
import com.example.controlii.databinding.LayoutBluetoothDevicesBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.math.sign


class MainActivity :
    AppCompatActivity(),
    BluetoothDeviceInterface,
    RequestBluetoothTurnOn,
    VisionDialog.SearchAction,
    View.OnClickListener,
    View.OnTouchListener,
    TuneUtilityDialog.OnTuneChange,
    BrightnessSlider.BrightnessSliderInterface
{



    private lateinit var connectedThread: ConnectedThread
    private lateinit var createConnectThread: CreateConnectThread



    var oldMillis = 0L
    //status pill


    lateinit var myStoreInfo: MyStoreInfo


    //notifications

    lateinit var notifManager: NotificationManagerCompat

    //..

    private lateinit var bondedList: ArrayList<DeviceInfoModel>
    private lateinit var bluetoothDeviceAdapter: BluetoothDeviceAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var manager: BluetoothManager

    private lateinit var handler: Handler



    /**VISUAL AND INTERACTION VIEWS**/



    /** CONTROLS **/
    private var isVerticalStickTouched = false
    private var isHorizontalStickTouched = false


    /**bluetooth related**/
    private lateinit var bonded: Set<BluetoothDevice>
    private var deviceName: String? = null
    private var deviceAddress: String? = null


    /**bindings**/
    private lateinit var amb: ActivityMainBinding
    private lateinit var ambp: ActivityMainStatusPillBinding
    private lateinit var ambd: LayoutBluetoothDevicesBinding

    /**global mm socket**/
    private lateinit var mmSocket: BluetoothSocket


    companion object {
        const val CONNECTING_STATUS = 1
        const val MESSAGE_STATUS = 2

        // 1 connected, -1 controller fails to con, -2 con disconnected
        const val CONNECTED = 1
        const val CONTROLLER_FAILS_TO_CONNECT = 2
        const val CONTROLLER_DISCONNECTED = 3
        const val CONTROLLER_RECONNECTING = 4


        //notification
        const val CHANNEL_ID = "controlii"

    }


    @SuppressLint(
        "MissingInflatedId",
        "SetJavaScriptEnabled",
        "MissingPermission",
        "ClickableViewAccessibility"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        amb = ActivityMainBinding.inflate(layoutInflater)
        ambp = amb.pill as ActivityMainStatusPillBinding
        ambd = amb.drawerHolder as LayoutBluetoothDevicesBinding
        ambd.apply {
            recyclerView.itemAnimator = null
        }

        setContentView(amb.root)


        myStoreInfo = MyStoreInfo(applicationContext).apply {
            writeInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS)
            writeInt(SELECTED_ITEM_POSITION, NO_POSITION)
        }


        IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            registerReceiver(bluetoothStateReceiver, this)
        }

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.container)
        }



        (amb.title.drawable as AnimatedVectorDrawable).apply {
            reset()
            Handler(Looper.getMainLooper()).postDelayed({
                start()
            }, 500)
        }
        amb.title.setOnClickListener {
            (amb.title.drawable as AnimatedVectorDrawable).start()
        }



        manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter




        /** click events and touch events  initialization **/
        this.let {
            amb.devicesButton.apply {
                setOnClickListener(it)
                setOnTouchListener(it)
            }
            amb.visionButton.apply {
                setOnClickListener(it)
                setOnTouchListener(it)
            }
            ambp.statusButton.apply {
                setOnClickListener(it)
            }



            amb.hornButton.apply {
                setOnClickListener(it)
                setOnTouchListener(it)
            }
            amb.cameraUpButton.apply {
                setOnClickListener(it)
                setOnTouchListener(it)
            }
            amb.cameraDownButton.apply {
                setOnClickListener(it)
                setOnTouchListener(it)
            }
            amb.flashlightButton.apply {
                setOnClickListener(it)
                setOnTouchListener(it)
                (drawable as AnimatedVectorDrawable).reset()
            }

            amb.cameraRightButton.apply {
                setOnClickListener(it)
                setOnTouchListener(it)
            }
            amb.cameraLeftButton.apply {
                setOnClickListener(it)
                setOnTouchListener(it)
            }


            amb.verticalLever.apply {
                setOnTouchListener(it)
                setOnSeekBarChangeListener(seekBarOnChange)
                (background as AnimatedVectorDrawable).reset()
            }
            amb.horizontalLever.apply {
                setOnTouchListener(it)
                setOnSeekBarChangeListener(seekBarOnChange)
                (background as AnimatedVectorDrawable).reset()
            }



            ambd.bluetoothSettings.setOnClickListener(it)
            ambd.dismissDrawer.setOnClickListener(it)
            ambd.bluetoothTitleHolder.setOnClickListener(it)

        }





        bluetoothTransmission.flashlightIntensity = myStoreInfo.readInt(TuneUtilityDialog.SEEKBAR_VAL, TuneUtilityDialog.DEFAULT_SEEKBAR_VAL)!!
        bluetoothTransmission.onTrigger()



        amb.flashlightButton.setOnLongClickListener {
            BrightnessSlider(this@MainActivity, this).show()
            true
        }



        onBackPress()

        initializeBluetoothDrawer()

        visionSet()





        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    CONNECTING_STATUS ->
                        when (msg.arg1) {
                            CONNECTED -> {
                                bluetoothTransmission.isEnableTransmission = true
                                handler.postDelayed({
                                    bluetoothTransmission.onTrigger()
                                }, 100)
                                myStoreInfo.writeInt(CONNECTION_STATUS_KEY, CONNECTED_STATUS)
                                bluetoothDeviceAdapter.notifyItemChanged(
                                    myStoreInfo.readInt(
                                        SELECTED_ITEM_POSITION,
                                        NO_POSITION
                                    )!!
                                )
                                statusPillTextAnim("ACTIVE")
                            }

                            CONTROLLER_FAILS_TO_CONNECT -> {

                                bluetoothTransmission.isEnableTransmission = false
                                myStoreInfo.writeInt(
                                    CONNECTION_STATUS_KEY,
                                    CONNECTION_FAILED_STATUS
                                )
                                bluetoothDeviceAdapter.notifyItemChanged(
                                    myStoreInfo.readInt(
                                        SELECTED_ITEM_POSITION,
                                        NO_POSITION
                                    )!!
                                )
                                statusPillTextAnim("FAILED")

                            }

                            CONTROLLER_DISCONNECTED -> {

                                bluetoothTransmission.isEnableTransmission = false
                                myStoreInfo.writeInt(CONNECTION_STATUS_KEY, DISCONNECTED_STATUS)
                                bluetoothDeviceAdapter.notifyItemChanged(
                                    myStoreInfo.readInt(
                                        SELECTED_ITEM_POSITION,
                                        NO_POSITION
                                    )!!
                                )
                                statusPillTextAnim("INACTIVE")

                            }

                            CONTROLLER_RECONNECTING -> {

                                bluetoothTransmission.isEnableTransmission  = false
                                myStoreInfo.writeInt(CONNECTION_STATUS_KEY, CONNECTING_STATUS_)
                                bluetoothDeviceAdapter.notifyItemChanged(
                                    myStoreInfo.readInt(
                                        SELECTED_ITEM_POSITION,
                                        NO_POSITION
                                    )!!
                                )
                                statusPillTextAnim("CONNECTING")

                            }

                        }
                }
            }
        }



    }


    @SuppressLint("MissingPermission")
    inner class CreateConnectThread(bluetoothAdapter: BluetoothAdapter, address: String) :
        Thread() {
        init {
            val bluetoothDevice: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            var tempSocket: BluetoothSocket? = null

            val uuid: UUID = bluetoothDevice.uuids[0].uuid
            try {
                tempSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (tempSocket != null) {
                mmSocket = tempSocket
            }
        }

        override fun run() {
            if (interrupted()) return
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

            bluetoothAdapter.cancelDiscovery()

            try {
                mmSocket.connect()
                Log.e("connection status:", "connected to device")
                handler.obtainMessage(CONNECTING_STATUS, CONNECTED, -1).sendToTarget()
            } catch (connectionException: IOException) {
                try {
                    mmSocket.close()
                    handler.obtainMessage(CONNECTING_STATUS, CONTROLLER_FAILS_TO_CONNECT, -1)
                        .sendToTarget()

                    Log.e("connection status: ", "cannot connect to device")
                } catch (closeException: IOException) {
                    Log.e("connection status: ", "cannot close client socket")
                }
                return
            }
            //TODO("connectedThread = ConnectedThread(mmSocket) connectedThread.run
            connectedThread = ConnectedThread(mmSocket)
            connectedThread.run()

        }

        fun cancel() {
            try {
                //your Device disconnected
                mmSocket.close()
                handler.obtainMessage(CONNECTING_STATUS, CONTROLLER_DISCONNECTED, -1).sendToTarget()
            } catch (e: IOException) {
                Log.e("", "cancel status: cannot cancel your device")
            }
        }


    }


    inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private var mmSocket: BluetoothSocket? = null
        private var mmInStream: InputStream? = null
        private var mmOutStream: OutputStream? = null

        init {
            mmSocket = socket
            var tempInputStream: InputStream? = null
            var tempOutputStream: OutputStream? = null

            try {
                tempInputStream = socket.inputStream
                tempOutputStream = socket.outputStream
            } catch (e: IOException) {
                /*************/
            }
            mmInStream = tempInputStream
            mmOutStream = tempOutputStream

        }

        override fun run() {
            if (interrupted()) return
            val buffer = ByteArray(1024)
            var index = 0

            while (true) {
                try {
                    buffer[index] = mmInStream!!.read().toByte()
                    val readMessage: String
                    if (buffer[index] == '\n'.code.toByte()) {
                        readMessage = String(buffer, 0, index)
                        handler.obtainMessage(MESSAGE_STATUS, readMessage).sendToTarget()
                        index = 0
                    } else {
                        index++
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }

        }

        fun write(input: String) {
            val bytes = input.toByteArray()
            try {
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {
                Log.e("MESSAGE:", "unable to send")
            }
        }

        fun cancel() {
            try {
                mmSocket!!.close()
                handler.obtainMessage(CONNECTING_STATUS, CONTROLLER_DISCONNECTED, -1).sendToTarget()
            } catch (e: IOException) {
                Log.e("MESSAGE:", "disconnection unsuccessfull")
            }
        }

    }


    @RequiresApi(VERSION_CODES.S)
    override fun onClick(view: View?) {
        when (view) {

            ambd.dismissDrawer -> {
                amb.drawerLayout.closeDrawer(GravityCompat.END)
            }

            ambd.bluetoothSettings, ambd.bluetoothTitleHolder -> {
                startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            }

            amb.devicesButton -> {

                val isBluetoothScanGranted =
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
                val isBluetoothConnectGranted =
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED

                val REQUEST_CODE = 1

                val request: (requestCode: Int, permission: String) -> Unit = { code, perm ->
                    ActivityCompat.requestPermissions(this, arrayOf(perm), code)
                }


                when {
                    !isBluetoothConnectGranted -> request(
                        REQUEST_CODE,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    )

                    !isBluetoothScanGranted -> request(
                        REQUEST_CODE,
                        android.Manifest.permission.BLUETOOTH_SCAN
                    )

                    else -> {
                        if (!amb.drawerLayout.isDrawerOpen(GravityCompat.END)){
                            amb.drawerLayout.openDrawer(
                                GravityCompat.END
                            )

                        }
                        else amb.drawerLayout.closeDrawer(GravityCompat.END)
                        showDevices()
                    }
                }
            }


            amb.visionButton -> {
                val dialog = VisionDialog()
                dialog.show(supportFragmentManager, "dialog")
            }


            amb.flashlightButton -> {

                val animatedVectorDrawable =
                    (view as ImageButton).drawable as AnimatedVectorDrawable

//                flashlightValue ++
//                if(flashlightValue > 1) flashlightValue = 0

                when (bluetoothTransmission.flashLightState) {
                    0 -> {
                        view.backgroundTintList = ContextCompat.getColorStateList(
                            this@MainActivity,
                            R.color.color_primary
                        )
                        animatedVectorDrawable.apply {
                            start()
                            setTint(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.color_on_primary
                                )
                            )
                        }

                        bluetoothTransmission.flashLightState = 1
                        bluetoothTransmission.onTrigger()
                    }

                    1 -> {


                        view.backgroundTintList =
                            ContextCompat.getColorStateList(this@MainActivity, R.color.container)
                        animatedVectorDrawable.apply {
                            reset()
                            setTint(ContextCompat.getColor(this@MainActivity, R.color.on_container))
                        }

                        bluetoothTransmission.flashLightState = 0
                        bluetoothTransmission.onTrigger()
                    }
                }
            }


            ambp.statusButton -> {

                val isOpened = ambp.statusText.visibility == View.VISIBLE
                if (isOpened) {
                    ambp.statusText.visibility = View.GONE
                    statusPillArrowAnim().reverse()
                } else {
                    ambp.statusText.visibility = View.VISIBLE
                    statusPillArrowAnim().start()
                }
            }

            ambd.bluetoothTitleHolder -> {

            }

        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {



        when (view) {

            amb.visionButton, amb.devicesButton -> {
                when (motionEvent?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.apply {
                            animate().apply {
                                scaleX(0.8f)
                                scaleY(0.8f)
                                duration = 60
                            }
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.apply {
                            animate().apply {
                                scaleX(1f)
                                scaleY(1f)
                                duration = 60
                            }


                        }
                    }
                }
                return !(motionEvent?.let { it.x in 0f..view.width.toFloat() && it.y in 0f..view.height.toFloat() })!!
            }


            amb.flashlightButton,
            amb.hornButton,
            amb.cameraUpButton,
            amb.cameraDownButton,
            amb.cameraRightButton,
            amb.cameraLeftButton-> {


                val animatedVectorDrawable
                        by lazy { (view as ImageButton).drawable as AnimatedVectorDrawable }

                when (motionEvent?.action) {

                    MotionEvent.ACTION_DOWN -> {


                        view.backgroundTintList = ContextCompat.getColorStateList(
                            this@MainActivity,
                            R.color.color_primary
                        )
                        (view as ImageButton).drawable.setTint(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.color_on_primary
                            )
                        )

                        if (view == amb.hornButton) animatedVectorDrawable.start()

                        view.apply {
                            animate().apply {
                                scaleX(0.92f)
                                scaleY(0.92f)
                                when (view) {
                                    amb.hornButton -> {
                                        pivotX = 0f
                                        pivotY = view.height.toFloat()/2
                                    }

                                    amb.cameraUpButton -> {
                                        pivotX = view.width.toFloat()/2
                                        pivotY = view.height.toFloat()
                                    }
                                    amb.cameraLeftButton -> {
                                        pivotX = view.width.toFloat()
                                        pivotY = view.height.toFloat()/2
                                    }

                                    amb.cameraRightButton -> {
                                        pivotX = 0f
                                        pivotY = view.height.toFloat()/2
                                    }

                                    amb.cameraDownButton -> {
                                        pivotX = view.width.toFloat()/2
                                        pivotY = 0f
                                    }


                                    amb.flashlightButton -> {
                                        pivotX = view.width.toFloat()
                                        pivotY = view.height.toFloat()/2


                                    }
                                }
                                duration = 40
                            }

                        }
                        when (view) {

                            amb.hornButton -> {
                                bluetoothTransmission.hornState = 1
                                bluetoothTransmission.onTrigger()
                            }

                            amb.cameraUpButton -> {
                                bluetoothTransmission.camUpState = 1
                                bluetoothTransmission.onTrigger()
                            }

                            amb.cameraDownButton -> {
                                bluetoothTransmission.camDownState = 1
                                bluetoothTransmission.onTrigger()
                            }

                            amb.cameraRightButton -> {
                                bluetoothTransmission.cameraRightState = 1
                                bluetoothTransmission.onTrigger()
                            }

                            amb.cameraLeftButton -> {
                                bluetoothTransmission.cameraLeftState = 1
                                bluetoothTransmission.onTrigger()
                            }
                        }
                        return false
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {


                        if (bluetoothTransmission.flashLightState == 0 || view != amb.flashlightButton) {
                            view.backgroundTintList = ContextCompat.getColorStateList(
                                this@MainActivity,
                                R.color.container
                            )
                            (view as ImageButton).drawable.setTint(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.on_container
                                )
                            )
                        }




                        if (view == amb.hornButton) animatedVectorDrawable.reset()

                        view.apply {
                            animate().apply {
                                scaleX(1f)
                                scaleY(1f)
                                duration = 40
                            }

                        }


                        when (view) {

                            amb.hornButton -> {
                                bluetoothTransmission.hornState = 0
                                bluetoothTransmission.onTrigger()
                            }

                            amb.cameraUpButton -> {
                                bluetoothTransmission.camUpState = 0
                                bluetoothTransmission.onTrigger()
                            }

                            amb.cameraDownButton -> {
                                bluetoothTransmission.camDownState = 0
                                bluetoothTransmission.onTrigger()
                            }

                            amb.cameraRightButton -> {
                                bluetoothTransmission.cameraRightState = 0
                                bluetoothTransmission.onTrigger()
                            }

                            amb.cameraLeftButton -> {
                                bluetoothTransmission.cameraLeftState = 0
                                bluetoothTransmission.onTrigger()
                            }

                        }

                        return !(motionEvent.let { it.x in 0f..view.width.toFloat() && it.y in 0f..view.height.toFloat() })

                    }

                }
            }


            amb.verticalLever -> {
                val thumbBounds = amb.verticalLever.thumb.bounds
                val offset = 35.dp

                when (motionEvent?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        /**Action down is the first action to occur when everytime onTouch is triggered.
                         * It is triggered only once for every session.
                         * This is a good opportunity to capture weather finger is touching the thumb or not
                         * the second action might be action up or action move,
                         * they will return false or true to determine if touch event is dispatched or not
                         * */
                        isVerticalStickTouched =
                            motionEvent.x.toInt() in thumbBounds.left + offset..thumbBounds.right - offset
                                    && motionEvent.y.toInt() in thumbBounds.top + offset..thumbBounds.bottom - offset
                        /**when returning the not of the logic, this will give as that thumb is touched
                         * but will return false, false means this listener will not be the last one to handle this event
                         * **/
                        return !isVerticalStickTouched
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        return !isVerticalStickTouched
                    }

                    MotionEvent.ACTION_MOVE -> {

                        return !isVerticalStickTouched
                    }

                    else -> {
                        return true
                    }
                }
            }


            amb.horizontalLever -> {
                val thumbBounds = amb.horizontalLever.thumb.bounds
                val offset = 35.dp

                when (motionEvent?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        /**Action down is the first action to occur when everytime onTouch is triggered.
                         * It is triggered only once for every session.
                         * This is a good opportunity to capture weather finger is touching the thumb or not
                         * the second action might be action up or action move,
                         * they will return false or true to determine if touch event will still occur in the seekbar
                         * */
                        isHorizontalStickTouched =
                            motionEvent.x.toInt() in thumbBounds.left + offset..thumbBounds.right - offset
                                    && motionEvent.y.toInt() in thumbBounds.top + offset..thumbBounds.bottom - offset
                        /**when returning the not of the logic, this will give as that thumb is touched
                         * but will return false, false means this listener will not be the last one to handle this event
                         * **/
                        return !isHorizontalStickTouched
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        return !isHorizontalStickTouched
                    }

                    MotionEvent.ACTION_MOVE -> {

                        return !isHorizontalStickTouched
                    }

                    else -> {
                        return true
                    }

                }


            }


        }
        return false
    }


    private val seekBarOnChange = object : SeekBar.OnSeekBarChangeListener {
        val vertBG by lazy { amb.verticalLever.background as AnimatedVectorDrawable }
        val horBG by lazy { amb.horizontalLever.background as AnimatedVectorDrawable }
        override fun onProgressChanged(seekbar: SeekBar?, value: Int, fromUser: Boolean) {
            if (fromUser) {
                when (seekbar) {
                    amb.horizontalLever -> {
                        bluetoothTransmission.horizontalValue = value
                    }

                    amb.verticalLever -> {
                        bluetoothTransmission.verticalValue = value
                    }
                }
                bluetoothTransmission.onTrigger()

            }
        }

        override fun onStartTrackingTouch(seekbar: SeekBar?) {
            when (seekbar) {
                amb.horizontalLever -> {
                    horBG.start()
                }

                amb.verticalLever -> {
                    amb.verticalLever.progress = 0
                    vertBG.start()
                }
            }
        }

        override fun onStopTrackingTouch(seekbar: SeekBar?) {
            when (seekbar) {
                amb.horizontalLever -> {
                    amb.horizontalLever.progress = 0
                    bluetoothTransmission.horizontalValue = 0
                    horBG.reset()
                }

                amb.verticalLever -> {
                    amb.verticalLever.progress = 0
                    bluetoothTransmission.verticalValue = 0
                    vertBG.reset()
                }
            }
            bluetoothTransmission.onTrigger()
        }

    }


    fun initializeBluetoothDrawer() {
        drawerToggle =
            object : ActionBarDrawerToggle(
                this,
                amb.drawerLayout,
                null,
                R.string.drawer_open,
                R.string.drawer_close
            ) {
                @SuppressLint("MissingPermission", "ClickableViewAccessibility")
                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)

                }

                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)

                }


            }
        amb.drawerLayout.addDrawerListener(drawerToggle)

    }

    @SuppressLint("SetJavaScriptEnabled")
    fun visionSet() {
        amb.webView.webViewClient = WebViewClient()
        val settings = amb.webView.settings
        settings.javaScriptEnabled = true

        amb.webView.loadUrl(myStoreInfo.readString(URL_KEY, DEFAULT_URL)!!)

        amb.swipeRefresh.refreshing = true

        amb.swipeRefresh.setOnRefreshListener(object : CustomRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                amb.webView.loadUrl(myStoreInfo.readString(URL_KEY, DEFAULT_URL)!!)
            }

        })


        amb.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                amb.swipeRefresh.refreshing = false
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                myStoreInfo.writeString(URL_KEY, view?.url.toString())

            }

            override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                return true
            }
        }
    }

    override fun OnSearchActionListener(url: String) {
        amb.webView.loadUrl(url)
        myStoreInfo.writeString(URL_KEY, url)
    }


    @SuppressLint("SuspiciousIndentation", "CommitPrefEdits")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {

            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {


                val alertDialogBuilder =
                    AlertDialog.Builder(this@MainActivity, R.style.TransparentDialogTheme).apply {
                        setMessage("As if this app is functional without Bluetooth. Go allow Nearby Devices Permission!!!")
                        setNegativeButton("HELL NO!") { _, _ ->
                        }
                        setPositiveButton("OK!") { _, _ ->
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                                startActivity(this)
                            }
                        }
                    }

                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_color))
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_color))

            }

        }
    }


    private fun onBackPress() {
        onBackPressedDispatcher.addCallback(this) {
            if (amb.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                amb.drawerLayout.closeDrawer(GravityCompat.END)
                return@addCallback
            }
            if (amb.webView.canGoBack()) {
                amb.webView.goBack()
            } else {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }

        }
    }


    override fun attachBaseContext(newBase: Context?) {
        val newOverride = Configuration(newBase?.resources?.configuration)
        newOverride.fontScale = 1.0f
        applyOverrideConfiguration(newOverride)
        super.attachBaseContext(newBase)
    }


    @SuppressLint("MissingPermission")
    private fun showDevices() {
        if (bluetoothAdapter.isEnabled) {
            bondedList = arrayListOf()
            bonded = bluetoothAdapter.bondedDevices

            bonded.forEach { device ->
                val i = DeviceInfoModel(device.name, device.address, device)
                if (!bondedList.any { it.getAddress == i.getAddress }) {
                    bondedList.add(i)
                }

            }

            bluetoothDeviceAdapter = BluetoothDeviceAdapter(bondedList, this, this, this)

            ambd.recyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = bluetoothDeviceAdapter
            }
        } else {
            Toast.makeText(this@MainActivity, "BLUETOOTH IS OFF", 2000).show()
        }
    }


    private fun statusPillTextAnim(message: String) {
        ambp.statusText.apply {
            text = message
            visibility = View.GONE
            statusPillArrowAnim().reverse()
            handler.postDelayed({
                visibility = View.VISIBLE
                statusPillArrowAnim().start()
            }, 300)
        }
    }

    private fun statusPillArrowAnim(): ValueAnimator {

        return ValueAnimator.ofFloat(90f, 270f).apply {
            duration = 150
            addUpdateListener { animator ->
                ambp.statusArrow.rotation = animator.animatedValue as Float

            }
        }
    }


    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission", "MissingInflatedId", "SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            val bluetoothState =
                intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when (bluetoothState) {
                BluetoothAdapter.STATE_ON -> {
                    showDevices()
                }

                BluetoothAdapter.STATE_OFF -> {
                    ambd.recyclerView.adapter = null
                    deviceAddress?.nonNullDoSome {
                        connectedThread.cancel()
                    }
                }
            }
            when (intent?.action) {
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    handler.obtainMessage(
                        CONNECTING_STATUS,
                        CONTROLLER_DISCONNECTED,
                        -1
                    ).sendToTarget()

                    showDevices()
                }
            }

            val uiModeManager = context?.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager

            when (uiModeManager.nightMode) {

                UiModeManager.MODE_NIGHT_AUTO,
                UiModeManager.MODE_NIGHT_CUSTOM,
                UiModeManager.MODE_NIGHT_NO,
                UiModeManager.MODE_NIGHT_YES -> {
                    if (bluetoothAdapter.isEnabled) showDevices()
                }
            }
        }

    }


    private val bluetoothEnable = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: androidx.activity.result.ActivityResult ->


    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothStateReceiver)

        myStoreInfo.writeInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS)
        myStoreInfo.writeInt(SELECTED_ITEM_POSITION, NO_POSITION)

    }


    override fun onClickBluetoothConnectListener(name: String, address: String) {
        deviceName = name
        deviceAddress = address

        createConnectThread = CreateConnectThread(bluetoothAdapter, deviceAddress.toString())
        handler.postDelayed({
            createConnectThread.start()
        }, 1000)


        handler.obtainMessage(CONNECTED_STATUS, CONTROLLER_RECONNECTING, -1).sendToTarget()

    }

    override fun onClickBluetoothDisconnectListener() {
        connectedThread.cancel()
        connectedThread.interrupt()
        myStoreInfo.writeInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS)
    }

    override fun onClickBluetoothCancelListener() {
        createConnectThread.cancel()
        createConnectThread.interrupt()
        myStoreInfo.writeInt(CONNECTION_STATUS_KEY, DEFAULT_STATUS)
    }


    override fun callBluetoothTurnOnRequest(intent: Intent) {
        bluetoothEnable.launch(intent)
    }

    override fun onTuneValChangeListener(tuneVal: Int) {

    }
    override fun onBrightnessSliderChange(value: Int) {
        bluetoothTransmission.flashlightIntensity = value
        bluetoothTransmission.onTrigger()
    }

    private val bluetoothTransmission = object : BluetoothTransmission {


        var flashLightState = 0
        var flashlightIntensity = 0
        var hornState = 0
        var camUpState = 0
        var camDownState = 0
        var cameraRightState = 0
        var cameraLeftState = 0
        var horizontalValue = 0
        var verticalValue = 0
        var isEnableTransmission = false


        private var leftTrack = 0
        private var rightTrack = 0
        private var isForward = false
        private var isBackward = false
        private var isRightBias = false
        private var isLeftBias = false

        fun yBias(isForward: Boolean, isBackward: Boolean) {
            this.isForward = isForward
            this.isBackward = isBackward
        }

        fun xBias(isLeftBias: Boolean, isRightBias: Boolean) {
            this.isLeftBias = isLeftBias
            this.isRightBias = isRightBias
        }



        override fun onTrigger() {
            when {
                verticalValue > 0 -> {
                    yBias(isForward = true, isBackward = false)
                }

                verticalValue < 0 -> {
                    yBias(isForward = false, isBackward = true)
                }

                else -> {
                    yBias(isForward = false, isBackward = false)
                }
            }
            when {
                horizontalValue > 0 -> {
                    xBias(isLeftBias = false, isRightBias = true)
                }

                horizontalValue < 0 -> {
                    xBias(isLeftBias = true, isRightBias = false)
                }

                else -> {
                    xBias(isLeftBias = false, isRightBias = false)
                }
            }


            when {

                isForward || isBackward -> {

                    when {
                        isLeftBias -> {
                            rightTrack = verticalValue
                            leftTrack = verticalValue -
                                    map(
                                        horizontalValue,
                                        0,
                                        horizontalValue.sign * 65536,
                                        0,
                                        verticalValue
                                    ).toInt()
                        }

                        isRightBias -> {
                            leftTrack = verticalValue
                            rightTrack = verticalValue -
                                    map(
                                        horizontalValue,
                                        0,
                                        horizontalValue.sign * 65536,
                                        0,
                                        verticalValue
                                    ).toInt()
                        }
                        /**this will trigger the top level only when statemanet
                         * allowing a forward or backward only motion
                         * **/
                        else -> {
                            leftTrack = verticalValue
                            rightTrack = verticalValue
                        }
                    }
                }

                isLeftBias || isRightBias -> {
                    leftTrack = horizontalValue
                    rightTrack = -horizontalValue
                }

                else -> {
                    leftTrack = 0
                    rightTrack = 0
                }

            }

            val toSend =
                "${leftTrack}:${rightTrack}:${flashLightState}:${hornState}:${camUpState}:${camDownState}:${cameraLeftState}:${cameraRightState}:${flashlightIntensity}\n"
            Log.i("sendVal", toSend)
            if (isEnableTransmission) {
                connectedThread.write(toSend)
            }
        }
    }




}


class MyStoreInfo(context: Context) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(
            folder,
            Context.MODE_PRIVATE
        )
    }
    private val folder = FOLDER_NAME


    fun writeString(key: String, value: String) {
        val edit = sharedPreferences.edit()
        edit.putString(key, value.toString())
        edit.apply()
    }

    fun writeInt(key: String, value: Int) {
        val edit = sharedPreferences.edit()
        edit.putInt(key, value)
        edit.apply()
    }

    fun readString(key: String, defaultValue: String): String? {
        return sharedPreferences.getString(key, defaultValue.toString())
    }

    fun readInt(key: String, defaultValue: Int): Int? {
        return sharedPreferences.getInt(key, defaultValue)
    }

    companion object {
        private const val FOLDER_NAME = "FOLDER-XP-ST-PAPA"

        //adapter-position Constants
        const val SELECTED_ITEM_POSITION = "selectedItemPosition"
        const val PRESERVE_ITEM_POSITION = "preservedItemPosition"
        const val NO_POSITION = RecyclerView.NO_POSITION

        //BLUETOOTH CONNECTION CONSTANTS
        const val CONNECTED_STATUS = 1
        const val DISCONNECTED_STATUS = 2
        const val CONNECTING_STATUS_ = 3
        const val CONNECTION_FAILED_STATUS = 4
        const val DEFAULT_STATUS = 5

        //LABEL FOR ITEMS IN RECYLERVIEW
        const val ACTIVE = "Active"
        const val CONNECTING_ = "Connecting..."


        //BLUETOOTH KEY CONSTANTS
        const val CONNECTION_STATUS_KEY = "CONNECTION_STATUS"


    }


}


inline fun <T, R> T.nonNullDoSome(block: (T) -> R): R {
    return block(this)
}

fun map(x: Int, in_min: Int, in_max: Int, out_min: Int, out_max: Int): Float {
    return ((x - in_min) * ((out_max - out_min).toFloat() / (in_max - in_min))) + out_min

}
















