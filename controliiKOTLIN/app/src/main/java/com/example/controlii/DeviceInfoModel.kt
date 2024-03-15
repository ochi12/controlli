package com.example.controlii

import android.bluetooth.BluetoothDevice

data class DeviceInfoModel(var getName: String?,
                           var getAddress: String?,
                           var bluetoothDevice: BluetoothDevice)

//data class will store values that are specified in its parameter/s