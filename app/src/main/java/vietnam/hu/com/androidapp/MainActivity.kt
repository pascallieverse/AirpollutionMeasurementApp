package vietnam.hu.com.androidapp


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import vietnam.hu.com.androidapp.service.BluetoothService
import java.util.*




class MainActivity : BaseActivity() {

    private var myReceiver: MyReceiver? = null
    private var locationService: Boolean = false


    /**
     *   Starts the main activity
     *
     *   Also start the bluetooth service
     *   Check permissions
     *   And register for the api if there is no account
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param DATAPASSED <String> json array with measurement data
     *
     *   @return <Void>
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        Fabric.with(this, Crashlytics())

        val sp = getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        this.title = "Home"
        
        
        //check permissions
        checkpermissions()

        //start running location service
        receiveLocations()
        val ha = Handler()
        ha.postDelayed(object : Runnable {

            override fun run() {
                //call function
                if(!locationService) {
                    receiveLocations()
                }
                ha.postDelayed(this, 30000)
            }
        }, 300000)

        layout_stub.layoutResource = R.layout.content_main
        val inflated = layout_stub.inflate()


        //Register BroadcastReceiver
        //to receive event from our service
        myReceiver = MyReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothService.SEND_DATA)
        registerReceiver(myReceiver, intentFilter)


        //start bluetooth service
        Log.d("VIETNAM", "TRY START INTENT SERVICE")

        if(sp.getString("device", "") != "" && sp.getString("device", "") != null) {
            var serviceIntent = Intent(this, BluetoothService::class.java)
            startService(serviceIntent)
        }


        //check if there is a account registered for the api

        if(!sp.getBoolean("apiRegistered", false)){
            registerForApi()
        }

        deviceInformation.text = "No connection"
        sensorData.text = "0.000"

    }

    //define the listener
    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d("VIETNAM", "" + location.longitude + ":" + location.latitude)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    /**
     *   Receive lng and lat locations
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Void>
     *
     */
    private fun receiveLocations(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationService = true
            Log.d("VIETNAM", "Starting new service for location here <----------------")
            var lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)

        }
    }


    /**
     *   Check if the app has all the permissions
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Void>
     *
     */
    private fun checkpermissions(){
        Log.d("VIETNAM", "check permissions")
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("VIETNAM", "No location permissions")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1111)
        }
    }


    /**
     *   Create's an account for the api server.
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Void>
     *
     */
    private fun registerForApi(){
        val queue = Volley.newRequestQueue(this)

        val jObject: JSONObject = JSONObject()
        var uuid: String = UUID.randomUUID().toString()
        var password : String = UUID.randomUUID().toString()  + UUID.randomUUID().toString()

        getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE).edit().putString("password", password).commit()

        jObject.put("username", uuid)
        jObject.put("password", password)
        jObject.put("email", "$uuid@projectvietnam.nl")

        Log.e("VIETNAM", uuid)

        try{
            val request = JsonObjectRequest(Request.Method.POST, Helper().getBaseUrl() + "/Users", jObject, Response.Listener<JSONObject> { response->
                val sp = getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE)
                var ed = sp.edit()

                ed.putString("username", response.getString("username"))
                ed.putString("email", response.getString("email"))
                ed.putString("userId", response.getString("id"))
                ed.putBoolean("apiRegistered", true)
                ed.commit();
            }, Response.ErrorListener { error ->
                    Log.e("VIETNAM", "Error: " + error.message)
                val handler = Handler()
                handler.postDelayed({
                    registerForApi()
                }, 60000)
            })
            queue.add(request)
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onStop() {
        try {
            unregisterReceiver(myReceiver)
        }
        catch(e: Exception){
            e.printStackTrace()
        }
        super.onStop()
    }

    override fun onResume() {
        deviceInformation.text = "No connection"
        sensorData.text = "0.000"

        //Register BroadcastReceiver
        //to receive event from our service
        myReceiver = MyReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothService.SEND_DATA)
        registerReceiver(myReceiver, intentFilter)
        super.onResume()
    }

    /**
     *   Receives broadcast from the bluetooth service with measurement information
     *   and put them in the frontend
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param DATAPASSED <String> json array with measurement data
     *
     *   @return <Void>
     *
     */
    inner class MyReceiver:BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1:Intent) {
            val datapassed = arg1.getStringExtra("DATAPASSED")

            if(datapassed == "0.000"){
                sensorData.text = "0.000"
                sensorData2.text = "0.000"
                sensorData3.text = "0.000"
            }
            else if(datapassed == "connected"){
                deviceInformation.text = "Connected"
            }
            else if(datapassed == "disconnected"){
                deviceInformation.text = "No connection"
            }
            else {
                val json = JSONObject(datapassed)

                if (json.has("value")) {
                    val value = json.getString("value")
                    if (value != null) {
                        deviceInformation.text = "Connected"
                        sensorData.text = json.getString("value")
                    }
                }
                if(json.has("pm10") && json.has("pm25")) {
                    sensorData2.text = json.getString("pm10")
                    sensorData3.text = json.getString("pm25")
                }
            }
        }
    }





}
