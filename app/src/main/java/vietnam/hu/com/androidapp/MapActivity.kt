package vietnam.hu.com.androidapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.ViewStub
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.HashMap

/**
 * Created by Pascal on 11/5/2017.
 */

class MapActivity : BaseActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
//    private var list: List<LatLng> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        val bluetooth = BluetoothAdapter.getDefaultAdapter()

        super.onCreate(savedInstanceState)
        this.title = "Map"

        val stub = findViewById<View>(R.id.layout_stub) as ViewStub
        stub.layoutResource = R.layout.content_map
        val inflated = stub.inflate()


        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    /**
     *   When map is ready, set the camera and call to receive data
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param googleMap <GoogleMap>
     *
     *   @return <Void>
     *
     */
    override fun onMapReady(googleMap: GoogleMap) {

        var location = getLocation()
        if(location != null) {
            var lat = location.latitude
            var lng = location.longitude
            if(lat != null && lng != null) {
                mMap = googleMap

                // Add a marker to current location
                val currentLocation = LatLng(lat, lng)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap!!.isMyLocationEnabled = true
                }

                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14F))

                getHeatmapData(lat, lng)
            }
        }
    }


    /**
     *   Request location
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Location>
     *
     */
    private fun getLocation() : Location? {
        var location: Location? = null
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val sp = getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE)
            var lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if(lm != null) {
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            else{Log.e("VIETNAM", "LocationManager empty")}
        }
        else{Log.d("VIETNAM", "No location permission")}
        return location
    }


    /**
     *   Request heatmap data and add it to the map
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param lat <Double> Latitude
     *   @param lng <Double> longitude
     *   @return <Boolean>
     *
     */

    private fun getHeatmapData(lat: Double, lng: Double){
        Helper().getToken(this, { token ->
            Log.d("VIETNAM", "Token: $token")

            val queue = Volley.newRequestQueue(this)

            val jObject = JSONObject()
            jObject.put("lat", lat)
            jObject.put("lon", lng)
            jObject.put("ratio", 50)
            jObject.put("ratio_point", 50)
            jObject.put("timerate", "day")

            val strRequest = object : JsonObjectRequest(Request.Method.POST, Helper().getBaseUrl() + "/Measurements/GetHeatmapDataWithRatio", jObject,
                    Response.Listener { response ->
                        Log.d("VIETNAM", "measurements response: " + response.toString())

                        val points = response.getJSONArray("points")


                        for (i in 0..(points.length() - 1)){
                            val list = ArrayList<LatLng>()
                            val lnglat = points.getJSONObject(i)
                            Log.d("VIETNAM", lnglat.get("lan").toString() +  " " + lnglat.get("long").toString())
                            val lat: Double = lnglat.get("lan").toString().toDouble()
                            val lng: Double = lnglat.get("long").toString().toDouble()
                            list.add(LatLng(lat, lng))


                            var colors: IntArray? = null


                            when(lnglat.get("poins")){
                                1 -> colors = intArrayOf(Color.rgb(23, 232, 23), Color.rgb(23, 232, 23))
                                2 -> colors = intArrayOf(Color.rgb(24, 205, 24), Color.rgb(24, 205, 24))
                                3 -> colors = intArrayOf(Color.rgb(27, 182, 27), Color.rgb(27, 182, 27))
                                4 -> colors = intArrayOf(Color.rgb(225, 229, 16), Color.rgb(225, 229, 16))
                                5 -> colors = intArrayOf(Color.rgb(227, 216, 13), Color.rgb(227, 216, 13))
                                6 -> colors = intArrayOf(Color.rgb(227, 205, 13), Color.rgb(227, 205, 13))
                                7 -> colors = intArrayOf(Color.rgb(233, 185, 12), Color.rgb(233, 185, 12))
                                8 -> colors = intArrayOf(Color.rgb(221, 169, 14), Color.rgb(221, 169, 14))
                                9 -> colors = intArrayOf(Color.rgb(206, 155, 13), Color.rgb(206, 155, 13))
                                10 -> colors = intArrayOf(Color.rgb(234, 119, 11), Color.rgb(234, 119, 11))
                                11 -> colors = intArrayOf(Color.rgb(219, 104, 15), Color.rgb(219, 104, 15))
                                12 -> colors = intArrayOf(Color.rgb(211, 99, 13), Color.rgb(211, 99, 13))
                                13 -> colors = intArrayOf(Color.rgb(225, 57, 14), Color.rgb(225, 57, 14))
                                14 -> colors = intArrayOf(Color.rgb(209, 38, 16), Color.rgb(209, 38, 16))
                                15 -> colors = intArrayOf(Color.rgb(194, 10, 10), Color.rgb(194, 10, 10))
                                else -> colors = intArrayOf(Color.rgb(0,255,0), Color.rgb(0,255,0))
                            }

                            val startPoints = floatArrayOf(0.2f, 1f)
                            val gradient = Gradient(colors, startPoints)

                            val mProvider = HeatmapTileProvider.Builder().data(list).gradient(gradient).build()
                            this.mMap!!.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))
                        }




                    },
                    Response.ErrorListener { error ->
                        Log.e("VIETNAM", "Something went wrong with the request")
                    }) {
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = token
                    return params
                }
            }


            strRequest.retryPolicy = DefaultRetryPolicy(30000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            queue.add(strRequest)
        })
    }


}
