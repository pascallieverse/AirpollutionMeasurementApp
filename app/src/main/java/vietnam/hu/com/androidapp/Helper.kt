package vietnam.hu.com.androidapp

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

/**
 * Created by Pascal on 1/26/2018.
 */

class Helper{


    /**
     *   Get the base url for the api server
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <String> api url
     *
     */
    fun getBaseUrl() : String{
        return "http://dev.api.projectvietnam.nl"
    }


    /**
     *   Get authentication token for the api server
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @param context <Context>
     *   @param callback <Lambda function> Callback function that runs after the request is done.
     *
     *   @return <Void>
     *
     */
    fun getToken(context: Context, callback: (id: String) -> Unit){
        val queue = Volley.newRequestQueue(context)
        val sp = context.getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE)

        val jObject: JSONObject = JSONObject()
        jObject.put("username", sp.getString("username", ""))
        jObject.put("password", sp.getString("password", ""))

        try{
            val request = JsonObjectRequest(Request.Method.POST, getBaseUrl()+"/Users/login", jObject, Response.Listener<JSONObject> { response->
                Log.d("VIETNAM", "Token request successful")
                callback(response.getString("id"))

            }, Response.ErrorListener { error ->
                Log.e("VIETNAM", String(error.networkResponse.data) )
            })
            queue.add(request)
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }


}