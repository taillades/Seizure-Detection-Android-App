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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.seizuredetection.GUI.LiveActivity.EXTRAS_DEVICE_ID
import ch.epfl.seizuredetection.R
import ch.epfl.seizuredetection.SignalClassifier
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


class MainActivity : AppCompatActivity() {

    private var recordingKeySaved: String? = null
    val USER_ID: String? = "USER_ID"
    private var play: ImageButton? = null
    private var deviceID: EditText? = null
    private var yesButton: Button? = null
    private var bluetooth: ImageButton? = null
    private var predictedTextView: TextView? = null
    private var profileButton: ImageButton? = null
    private var signalClassifier = SignalClassifier(this)
    private var firebasePerformance = FirebasePerformance.getInstance()
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private val BLE_CONNECTION = 1
    val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
    val EXTRAS_DEVIDE_ID = "DEVICE_ID"
    val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"
    private var mDeviceAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Setup view instances
        deviceID = findViewById(R.id.textDeviceID)
        play = findViewById<ImageButton>(R.id.play);
        play!!.setOnClickListener(View.OnClickListener {
            val intent: Intent = getIntent()
            val userID = intent.extras!!.getString(USER_ID)
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
                    var idd = deviceID!!.text
                    intentStartLive.putExtra(EXTRAS_DEVICE_ID, deviceID!!.text)
                    startActivity(intentStartLive)
                }
            })

        })

        // Toolbar action to Edit Profile
        profileButton = findViewById<ImageButton>(R.id.profile)
        profileButton!!.setOnClickListener {
            val intentProfile = Intent(this@MainActivity, EditProfileActivity::class.java)
            //intentProfile.putExtra(USER_ID, userID)
            startActivity(intentProfile)
            }

    }


    override fun onDestroy() {
        signalClassifier.close()
        super.onDestroy()
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
}
