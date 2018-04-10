package vietnam.hu.com.androidapp.service

import android.Manifest
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import vietnam.hu.com.androidapp.Helper
import vietnam.hu.com.androidapp.R
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * Created by Pascal on 23/11/2017.
 */

class BluetoothService : Service() {

    companion object {
    var SEND_DATA = "SEND_DATA"
    }

    private var token: String? = null
    private var requestQueue: RequestQueue? = null
    private val CUSTOM_SERVICE = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
    private val SENSOR1_UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB")
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var mGatt: BluetoothGatt? = null
    private val mGattCallback = object : BluetoothGattCallback() {

        private var outputString = ""


        /**
         *   When the connection is created start searching for services
         *
         *   @since 0.1
         *   @author Pascal Lieverse <pascallieverse@live.nl>
         *
         *   @param gatt <BluetoothGatt>
         *   @param status <Int>
         *
         *   @return <Void>
         *
         */
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("VIETNAM", "Connected to GATT client. Attempting to start service discovery")
                gatt.discoverServices()
                sendDataBroadcast("connected")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("VIETNAM", "Disconnected from GATT client")
                sendDataBroadcast("0.000")
                sendDataBroadcast("disconnected")
                //waith 10 seconds before trying to reconnect
                Thread.sleep(5000)
                startBluetoothConnection()
            }
        }

        /**
         *   When the connection is created connect to the service for the data
         *
         *   @since 0.1
         *   @author Pascal Lieverse <pascallieverse@live.nl>
         *
         *   @param gatt <BluetoothGatt>
         *   @param status <Int>
         *
         *   @return <Void>
         *
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("VIETNAM", "No service found")
                return
            }
            Log.w("VIETNAM", "onServicesDiscovered received: " + status)
            Log.i("VIETNAM", "Connecting to service")

            // Get the counter characteristic
            val characteristic = gatt.getService(CUSTOM_SERVICE).getCharacteristic(SENSOR1_UUID)

            // Enable notifications for this characteristic locally
            gatt.setCharacteristicNotification(characteristic, true)
        }

        /**
         *   Read a piece of the received data
         *
         *   @since 0.1
         *   @author Pascal Lieverse <pascallieverse@live.nl>
         *
         *   @param characteristic <BluetoothGattCharacteristic> Bit array with a part of the data
         *
         *   @return <Void>
         *
         */
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            readCounterCharacteristic(characteristic)
        }

        /**
         *   Handle a piece of the received data
         *
         *   @since 0.1
         *   @author Pascal Lieverse <pascallieverse@live.nl>
         *
         *   @param characteristic <BluetoothGattCharacteristic> Bit array with a part of the data
         *
         *   @return <Void>
         *
         */
        private fun readCounterCharacteristic(characteristic: BluetoothGattCharacteristic) {
            if (SENSOR1_UUID == characteristic.uuid) {
                val data = characteristic.value
                val outputData = String(data, StandardCharsets.UTF_8)

                if (outputData.contains("{")) {
                    outputString = outputData
                } else if (outputData.contains("}")) {
                    outputString += outputData

                    if (outputString.contains("{")) {
                        handleData(outputString)
                    }
                } else {
                    outputString += outputData
                }
            }
        }
    }


    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        Log.d("VIETNAM", "STARTING bluetooth service")
        Helper().getToken(this, { token ->
            this.token = token
        })
        try {
            startBluetoothConnection()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onCreate()
    }

    /**
     *   Starts the service, connect to the bluetooth device.
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param intent <Intent>
     *   @param flags <Int>
     *   @param startId <Int>
     *
     *   @return <Int> Sticky
     *
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        return Service.START_NOT_STICKY
    }


    private fun startBluetoothConnection(){
        Log.d("VIETNAM", "Trying to start new bluetooth connection")

        if(mGatt != null){
            mGatt!!.close()
        }

        val sp = getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE)
        val device = bluetoothAdapter.getRemoteDevice(sp.getString("device", ""))
        this.mGatt = device.connectGatt(this, false, mGattCallback)
    }


    /**
     *   Handle the data that is just received from the bluetooth device
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param data <String> received data from the wearable
     *
     *   @return <Void>
     *
     */
    private fun handleData(data: String) {
        if (isJSONValid(data)) {
            var json = JSONObject(data)
            Log.d("VIETNAM", "sensor data: " + data)
            sendDataBroadcast(data)


            val sp = getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE)
            if(sp.getBoolean("apiRegistered", false) && sp.getBoolean("shareData", true)){
                shareData(json)
            }
        } else {
            Log.e("VIETNAM", "geen valide sensor data binnengekomen")
        }
    }

    /**
     *   Check if a string is a valid json array
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param data <String> received data from the wearable
     *
     *   @return <Boolean>
     *
     */
    private fun isJSONValid(test: String): Boolean {
        try {
            JSONObject(test)
        } catch (ex: JSONException) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                JSONArray(test)
            } catch (ex1: JSONException) {
                return false
            }

        }
        return true
    }

    /**
     *   Send a broadcast to the front view
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param data <String> received data from the wearable
     *
     *   @return <Void>
     *
     */
    private fun sendDataBroadcast(data: String){
        var intent = Intent()
        intent.action = SEND_DATA
        intent.putExtra("DATAPASSED", data)
        sendBroadcast(intent)
    }


    /**
     *   Send the received data to the backend
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param data <String> received data from the wearable
     *
     *   @return <Void>
     *
     */
    private fun shareData(json: JSONObject){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val sp = getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE)
            var lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if(lm != null && token != null) {
                var location: Location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                var lng = location.longitude
                var lat = location.latitude

                if (lng != null && lat != null) {
                    if (requestQueue == null) {
                        Log.d("VIETNAM","Creating new request que")
                        requestQueue = Volley.newRequestQueue(this)
                    }

                    val jObject = JSONObject()
                    val latLng = JSONObject()
                    latLng.put("lat", lat)
                    latLng.put("lng", lng)

                    jObject.put("location", latLng)
                    jObject.put("userid", sp.getString("userId", "0"))
                    if(json.has("value")) {
                        if(json.get("value") != null && json.get("value") != "") {
                            jObject.put("gas", json.get("value"))

                            if(json.has("pm10") && json.has("pm25")) {
                                jObject.put("pm10", json.get("pm10"))
                                jObject.put("pm25", json.get("pm25"))
                            }

                            val strRequest = object : JsonObjectRequest(Request.Method.POST, Helper().getBaseUrl() + "/Measurements", jObject,
                                    Response.Listener { response ->
                                        Log.d("VIETNAM", "measurements response: " + response.toString())
                                    },
                                    Response.ErrorListener { error ->
                                        Log.e("VIETNAM", "Something went wrong with the request")
                                    }) {
                                override fun getHeaders(): Map<String, String> {
                                    val params = HashMap<String, String>()
                                    params["Authorization"] = token!!
                                    return params
                                }
                            }
                            if (requestQueue != null) {
                                requestQueue!!.add(strRequest)
                            }
                        }
                    }
                }
                else{
                    Log.e("VIETNAM", "lng lat empty")
                }
            }
            else{
                Log.e("VIETNAM", "LocationManager empty")
            }
        }
        else{
            Log.d("VIETNAM", "No location permission")
        }
    }
}
