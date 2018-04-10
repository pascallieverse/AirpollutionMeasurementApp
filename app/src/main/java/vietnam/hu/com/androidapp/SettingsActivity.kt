package vietnam.hu.com.androidapp

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import android.preference.Preference.OnPreferenceClickListener
import vietnam.hu.com.androidapp.service.BluetoothService


/**
 * Created by Pascal on 11/1/2017.
 */

class SettingsActivity : PreferenceActivity() {
    private var mBtAdapter: BluetoothAdapter? = null
    private var devices = mutableMapOf<String, String>()
    private var tempKeys: Array<String>? = null


    /**
     *   Build up all the settings views and insert data into the setting value
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Void>
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sp = getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE)
        val ed = sp.edit()

        setContentView(R.layout.activity_settings)

        //check bluethooth support
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBtAdapter == null) {
            Toast.makeText(this, "No bluethooth support", Toast.LENGTH_LONG).show()
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mBtReceiver, filter)

        // Getting the Bluetooth adapter
        if (mBtAdapter != null) {
            mBtAdapter!!.startDiscovery()
            Toast.makeText(this, "Starting discovery...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth disabled or not available.", Toast.LENGTH_SHORT).show()
        }

        //set toolbar
        settings_toolbar.title = "Settings"
        settings_toolbar.setTitleTextColor(Color.parseColor("#ffffff"))
        settings_toolbar.setNavigationOnClickListener { finish() }


        //add device settings
        addPreferencesFromResource(R.xml.device)


        val myPref = findPreference("pref_key_device_list") as Preference
        myPref.onPreferenceClickListener = OnPreferenceClickListener {
            createListPreferenceDialog()
            false
        }

        //share device button
        val pref = findPreference("pref_key_sharing_device") as CheckBoxPreference
        pref.setDefaultValue(sp.getBoolean("shareData", true))
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            val checked = java.lang.Boolean.valueOf(newValue.toString())!!
            ed.putBoolean("shareData", checked)
            ed.commit()
            true
        }
    }

    /**
     *   Create bluetooth list
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Void>
     *
     */
    private fun createListPreferenceDialog() {
        val dialog: Dialog
        val b : AlertDialog.Builder = AlertDialog.Builder(this)

        tempKeys = devices.keys.toTypedArray()

        b.setTitle("Select Wearable")
        b.setItems(tempKeys, { dialogInterface, i -> setDevice(dialogInterface, i) })
        dialog = b.create()
        dialog.show()
    }

    /**
     *   Make new bluetooth connection after selection
     *
     *   @since 0.1
     *   @author Pascal Lieverse <pascallieverse@live.nl>
     *
     *   @return <Void>
     *
     */
    private fun setDevice(dialogInterface: DialogInterface, i: Int){
        val sp = getSharedPreferences("VIETNAM.HU.COM", Context.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putString("device", devices[tempKeys!![i]])
        ed.commit()

        var serviceIntent = Intent(this, BluetoothService::class.java)
        startService(serviceIntent)
    }

    private val mBtReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if(device.name != null){
                    devices[device.name] = device.address
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBtAdapter != null) {
            mBtAdapter!!.cancelDiscovery()
        }
        unregisterReceiver(mBtReceiver)
    }
}
