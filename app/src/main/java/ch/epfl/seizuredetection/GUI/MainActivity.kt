// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ch.epfl.seizuredetection.GUI

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.seizuredetection.R
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var recordingKeySaved: String? = null
    val USER_ID: String? = "USER_ID"
    private var play: ImageButton? = null
    private var deviceID: EditText? = null
    private var profileButton: ImageButton? = null
    private var backButton: ImageButton? = null
    val EXTRAS_DEVICE_ID = "DEVICE_ID"
    val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"
    val EXTRAS_COMPRESSION_RATE= "COMPRESSION_RATE"
    private var mDeviceAddress: String? = null
    private var selectedRate = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Setup view instances

        deviceID = findViewById(R.id.textDeviceID)
        play = findViewById<ImageButton>(R.id.play);

        val intent: Intent = getIntent()
        val userIDsq = intent.extras!!.getString("USER_ID_SQ")
        val userID = intent.extras!!.getString(USER_ID)

        play!!.setOnClickListener(View.OnClickListener {
            val database = FirebaseDatabase.getInstance()
            val profileGetRef = database.getReference("profiles")
            val recordingRef = profileGetRef.child(userID!!).child("recordings").push()
            recordingRef.runTransaction(object : Transaction.Handler {

                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    mutableData.child("datetime").value = System.currentTimeMillis()
                    recordingKeySaved = recordingRef.key
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {

                    val intentStartLive = Intent(this@MainActivity, LiveActivity::class.java)
                    intentStartLive.putExtra(USER_ID, userID)
                    RECORDING_ID = recordingKeySaved.toString()
                    intentStartLive.putExtra(RECORDING_ID, recordingKeySaved)
                    intentStartLive.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress)
                    intentStartLive.putExtra(EXTRAS_COMPRESSION_RATE, selectedRate)
                    var idd = deviceID!!.text
                    intentStartLive.putExtra(EXTRAS_DEVICE_ID, idd)
                    startActivity(intentStartLive)
                }
            })
        })

        // Toolbar action to Edit Profile
        profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton!!.setOnClickListener {
            val intentProfile = Intent(this@MainActivity, EditProfileActivity::class.java)
            intentProfile.putExtra("USER_ID_SQ", userIDsq)
            startActivity(intentProfile)
        }

        //Remove going back button from toolbar
        backButton = findViewById<ImageButton>(R.id.backButton)
        val parent2 = backButton!!.parent as ViewGroup
        parent2.removeView(backButton)


        val compressionRateSelector: Spinner = findViewById(R.id.compressionRateSelector)
        ArrayAdapter.createFromResource(
                this,
                R.array.rate_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            compressionRateSelector.adapter = adapter
        }
        class SpinnerActivity : Activity(), AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                selectedRate = parent.getItemAtPosition(pos) as Int
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        compressionRateSelector.onItemSelectedListener = this
    }

    private fun showToast(text: String) {
        Toast.makeText(
                this,
                text,
                Toast.LENGTH_LONG
        ).show()
    }

    /*
         override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == BLE_CONNECTION && resultCode == RESULT_OK) {
                if (data != null) {
                    mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS)
                }
            }
        }
    */
    companion object {
        lateinit var RECORDING_ID: String
        private const val TAG = "MainActivity"
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}


