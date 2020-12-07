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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.seizuredetection.R
import ch.epfl.seizuredetection.SignalClassifier
import com.google.android.gms.tasks.Task
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

    private var yesButton: Button? = null
    private var predictedTextView: TextView? = null
    private var signal: FloatArray = floatArrayOf((6.4).toFloat(), (-4.5).toFloat(), (-6.9).toFloat(), (-4.6).toFloat(), (9.8).toFloat(), (-0.6).toFloat(), (1.5).toFloat(), (2.8).toFloat(), (-9.4).toFloat(), (-3.6).toFloat(), (6.6).toFloat(), (9.0).toFloat(), (-3.7).toFloat(), (-6.3).toFloat(), (-3.4).toFloat(), (-0.6).toFloat(), (-9.5).toFloat(), (1.3).toFloat(), (4.9).toFloat(), (-4.6).toFloat(), (7.2).toFloat(), (7.0).toFloat(), (3.8).toFloat(), (5.9).toFloat(), (-3.9).toFloat(), (4.0).toFloat(), (-5.7).toFloat(), (-4.1).toFloat(), (4.4).toFloat(), (7.4).toFloat(), (-6.5).toFloat(), (5.9).toFloat(), (2.8).toFloat(), (-0.8).toFloat(), (-0.7).toFloat(), (-5.5).toFloat(), (-9.5).toFloat(), (5.3).toFloat(), (5.7).toFloat(), (-4.6).toFloat(), (7.8).toFloat(), (7.2).toFloat(), (-3.1).toFloat(), (2.9).toFloat(), (3.2).toFloat(), (6.7).toFloat(), (5.4).toFloat(), (-5.5).toFloat(), (-1.1).toFloat(), (3.5).toFloat(), (-0.9).toFloat(), (-4.0).toFloat(), (-7.5).toFloat(), (6.3).toFloat(), (-0.5).toFloat(), (9.5).toFloat(), (5.7).toFloat(), (2.5).toFloat(), (-6.7).toFloat(), (2.0).toFloat(), (4.9).toFloat(), (4.9).toFloat(), (-9.9).toFloat(), (-0.2).toFloat(), (-8.9).toFloat(), (-9.6).toFloat(), (5.2).toFloat(), (-6.9).toFloat(), (0.7).toFloat(), (-1.9).toFloat(), (-0.5).toFloat(), (3.8).toFloat(), (2.4).toFloat(), (-0.9).toFloat(), (-5.1).toFloat(), (-1.7).toFloat(), (-2.9).toFloat(), (-9.3).toFloat(), (2.5).toFloat(), (8.8).toFloat(), (-8.2).toFloat(), (0.9).toFloat(), (-7.8).toFloat(), (1.5).toFloat(), (8.9).toFloat(), (9.0).toFloat(), (-6.9).toFloat(), (-8.9).toFloat(), (-2.5).toFloat(), (-9.1).toFloat(), (-9.5).toFloat(), (3.6).toFloat(), (7.8).toFloat(), (-7.4).toFloat(), (10.0).toFloat(), (-1.0).toFloat(), (-4.2).toFloat(), (-5.1).toFloat(), (6.8).toFloat(), (-6.3).toFloat(), (-4.7).toFloat(), (1.5).toFloat(), (-6.9).toFloat(), (7.1).toFloat(), (-9.0).toFloat(), (9.5).toFloat(), (-7.9).toFloat(), (-3.8).toFloat(), (-2.2).toFloat(), (5.2).toFloat(), (3.4).toFloat(), (4.0).toFloat(), (2.7).toFloat(), (-2.1).toFloat(), (-9.7).toFloat(), (6.5).toFloat(), (-7.4).toFloat(), (-9.3).toFloat(), (0.2).toFloat(), (-3.1).toFloat(), (3.7).toFloat(), (1.8).toFloat(), (-5.3).toFloat(), (8.7).toFloat(), (9.5).toFloat(), (-2.8).toFloat(), (-3.8).toFloat(), (-9.6).toFloat(), (-6.2).toFloat(), (6.6).toFloat(), (8.9).toFloat(), (-1.5).toFloat(), (-3.6).toFloat(), (-4.4).toFloat(), (1.2).toFloat(), (-8.7).toFloat(), (-8.6).toFloat(), (-9.5).toFloat(), (-6.6).toFloat(), (-2.7).toFloat(), (5.7).toFloat(), (-0.2).toFloat(), (-4.2).toFloat(), (4.9).toFloat(), (3.6).toFloat(), (0.1).toFloat(), (6.7).toFloat(), (8.4).toFloat(), (6.6).toFloat(), (2.6).toFloat(), (-7.2).toFloat(), (-0.2).toFloat(), (-7.6).toFloat(), (5.4).toFloat(), (-5.0).toFloat(), (1.0).toFloat(), (-0.5).toFloat(), (-2.8).toFloat(), (5.2).toFloat(), (1.0).toFloat(), (0.8).toFloat(), (9.1).toFloat(), (-5.8).toFloat(), (-4.9).toFloat(), (-2.9).toFloat(), (5.6).toFloat(), (7.2).toFloat(), (-6.4).toFloat(), (2.5).toFloat(), (1.5).toFloat(), (4.8).toFloat(), (9.6).toFloat(), (-3.9).toFloat(), (4.1).toFloat(), (-2.2).toFloat(), (-3.1).toFloat(), (3.3).toFloat(), (-2.8).toFloat(), (-8.6).toFloat(), (-3.2).toFloat(), (-3.0).toFloat(), (8.9).toFloat(), (6.9).toFloat(), (3.9).toFloat(), (-4.9).toFloat(), (2.9).toFloat(), (-4.8).toFloat(), (-1.2).toFloat(), (1.8).toFloat(), (6.0).toFloat(), (-1.3).toFloat(), (8.0).toFloat(), (-5.1).toFloat(), (-1.6).toFloat(), (-3.8).toFloat(), (6.4).toFloat(), (8.3).toFloat(), (8.0).toFloat(), (6.0).toFloat(), (7.9).toFloat(), (-9.6).toFloat(), (3.1).toFloat(), (2.8).toFloat(), (-3.6).toFloat(), (7.1).toFloat(), (-3.3).toFloat(), (2.4).toFloat(), (-2.6).toFloat(), (4.1).toFloat(), (9.8).toFloat(), (-0.3).toFloat(), (1.5).toFloat(), (-8.7).toFloat(), (3.8).toFloat(), (-3.6).toFloat(), (5.3).toFloat(), (-2.2).toFloat(), (-1.7).toFloat(), (-8.2).toFloat(), (2.5).toFloat(), (-5.2).toFloat(), (9.2).toFloat(), (2.6).toFloat(), (5.7).toFloat(), (-5.1).toFloat(), (-2.6).toFloat(), (-8.9).toFloat(), (5.1).toFloat(), (0.4).toFloat(), (-2.4).toFloat(), (3.1).toFloat(), (-5.2).toFloat(), (8.3).toFloat(), (8.5).toFloat(), (-6.5).toFloat(), (-7.9).toFloat(), (-2.5).toFloat(), (2.6).toFloat(), (-9.5).toFloat(), (2.4).toFloat(), (-8.2).toFloat(), (-2.2).toFloat(), (1.1).toFloat(), (-9.2).toFloat(), (-8.7).toFloat(), (7.6).toFloat(), (9.5).toFloat(), (-3.1).toFloat(), (2.0).toFloat(), (-1.0).toFloat(), (-4.1).toFloat(), (8.1).toFloat(), (-9.7).toFloat(), (1.4).toFloat(), (4.5).toFloat(), (-3.6).toFloat(), (3.4).toFloat(), (7.6).toFloat(), (-8.4).toFloat(), (-2.7).toFloat(), (-5.5).toFloat(), (5.7).toFloat(), (3.9).toFloat(), (7.3).toFloat(), (9.0).toFloat(), (-0.1).toFloat(), (0.9).toFloat(), (2.8).toFloat(), (-8.8).toFloat(), (7.8).toFloat(), (6.4).toFloat(), (-0.2).toFloat(), (1.1).toFloat(), (6.9).toFloat(), (-3.5).toFloat(), (-8.0).toFloat(), (5.4).toFloat(), (2.5).toFloat(), (2.6).toFloat(), (7.4).toFloat(), (-8.1).toFloat(), (-9.3).toFloat(), (4.8).toFloat(), (9.0).toFloat(), (5.5).toFloat(), (4.0).toFloat(), (5.8).toFloat(), (-4.2).toFloat(), (6.1).toFloat(), (-2.5).toFloat(), (1.2).toFloat(), (8.8).toFloat(), (-1.1).toFloat(), (-4.7).toFloat(), (9.4).toFloat(), (9.4).toFloat(), (-4.8).toFloat(), (3.8).toFloat(), (-8.1).toFloat(), (-7.5).toFloat(), (-5.2).toFloat(), (0.8).toFloat(), (8.6).toFloat(), (-7.7).toFloat(), (-7.0).toFloat(), (8.5).toFloat(), (7.4).toFloat(), (-1.1).toFloat(), (5.3).toFloat(), (-9.1).toFloat(), (7.0).toFloat(), (9.0).toFloat(), (1.0).toFloat(), (7.4).toFloat(), (-7.9).toFloat(), (-7.8).toFloat(), (9.7).toFloat(), (4.6).toFloat(), (-1.7).toFloat(), (-8.0).toFloat(), (4.9).toFloat(), (0.5).toFloat(), (-5.7).toFloat(), (3.9).toFloat(), (-6.4).toFloat(), (-6.7).toFloat(), (9.8).toFloat(), (-0.8).toFloat(), (-0.6).toFloat(), (3.1).toFloat(), (-9.5).toFloat(), (3.6).toFloat(), (0.1).toFloat(), (-4.2).toFloat(), (-3.4).toFloat(), (-0.7).toFloat(), (-9.1).toFloat(), (3.4).toFloat(), (-9.7).toFloat(), (2.6).toFloat(), (-6.1).toFloat(), (-3.9).toFloat(), (-7.9).toFloat(), (-8.4).toFloat(), (8.6).toFloat(), (-4.4).toFloat(), (1.8).toFloat(), (7.2).toFloat(), (6.7).toFloat(), (9.6).toFloat(), (2.9).toFloat(), (5.3).toFloat(), (-9.3).toFloat(), (-2.0).toFloat(), (3.6).toFloat(), (4.9).toFloat(), (-2.7).toFloat(), (6.9).toFloat(), (9.4).toFloat(), (-3.7).toFloat(), (-3.6).toFloat(), (4.1).toFloat(), (9.6).toFloat(), (2.0).toFloat(), (5.7).toFloat(), (6.1).toFloat(), (7.2).toFloat(), (7.2).toFloat(), (7.1).toFloat(), (-8.8).toFloat(), (-7.2).toFloat(), (-5.4).toFloat(), (7.6).toFloat(), (-3.9).toFloat(), (0.2).toFloat(), (-0.2).toFloat(), (3.9).toFloat(), (2.4).toFloat(), (-0.2).toFloat(), (9.1).toFloat(), (-1.4).toFloat(), (-1.5).toFloat(), (-7.5).toFloat(), (9.5).toFloat(), (2.2).toFloat(), (-1.1).toFloat(), (-0.3).toFloat(), (-7.3).toFloat(), (7.1).toFloat(), (-1.0).toFloat(), (8.6).toFloat(), (6.9).toFloat(), (8.6).toFloat(), (-1.9).toFloat(), (1.1).toFloat(), (8.9).toFloat(), (1.8).toFloat(), (-4.5).toFloat(), (4.9).toFloat(), (10.0).toFloat(), (6.6).toFloat(), (5.4).toFloat(), (-7.3).toFloat(), (-0.4).toFloat(), (5.2).toFloat(), (-5.6).toFloat(), (0.5).toFloat(), (-5.2).toFloat(), (-3.2).toFloat(), (4.8).toFloat(), (-8.2).toFloat(), (-9.9).toFloat(), (2.9).toFloat(), (-2.5).toFloat(), (7.5).toFloat(), (5.8).toFloat(), (7.5).toFloat(), (1.4).toFloat(), (5.0).toFloat(), (-8.5).toFloat(), (-4.0).toFloat(), (-3.6).toFloat(), (7.2).toFloat(), (-4.2).toFloat(), (-2.0).toFloat(), (7.0).toFloat(), (4.4).toFloat(), (9.0).toFloat(), (3.4).toFloat(), (-9.1).toFloat(), (-7.2).toFloat(), (-3.3).toFloat(), (1.0).toFloat(), (2.0).toFloat(), (-5.3).toFloat(), (-5.4).toFloat(), (-4.5).toFloat(), (-2.0).toFloat(), (5.6).toFloat(), (-6.1).toFloat(), (1.8).toFloat(), (-7.5).toFloat(), (-8.4).toFloat(), (8.4).toFloat(), (-7.9).toFloat(), (8.4).toFloat(), (7.0).toFloat(), (-6.5).toFloat(), (9.7).toFloat(), (-2.4).toFloat(), (-9.2).toFloat(), (-8.9).toFloat(), (5.8).toFloat(), (0.4).toFloat(), (4.3).toFloat(), (2.8).toFloat(), (1.5).toFloat(), (2.4).toFloat(), (-7.2).toFloat(), (-6.6).toFloat(), (-3.5).toFloat(), (-0.1).toFloat(), (6.4).toFloat(), (-1.0).toFloat(), (0.4).toFloat(), (1.3).toFloat(), (3.6).toFloat(), (3.3).toFloat(), (-6.4).toFloat(), (-1.3).toFloat(), (8.7).toFloat(), (-0.8).toFloat(), (8.6).toFloat(), (6.3).toFloat(), (6.7).toFloat(), (-6.8).toFloat(), (4.3).toFloat(), (1.8).toFloat(), (5.5).toFloat(), (-8.1).toFloat(), (-7.4).toFloat(), (-7.5).toFloat(), (-6.6).toFloat(), (9.4).toFloat(), (0.6).toFloat(), (-7.7).toFloat(), (4.2).toFloat(), (-0.4).toFloat(), (-3.6).toFloat(), (-8.2).toFloat(), (-2.4).toFloat(), (2.5).toFloat(), (-2.5).toFloat(), (-6.6).toFloat(), (-0.8).toFloat(), (3.0).toFloat(), (-9.6).toFloat(), (7.5).toFloat(), (-2.6).toFloat(), (4.1).toFloat(), (-0.0).toFloat(), (-0.4).toFloat(), (2.6).toFloat(), (-3.0).toFloat(), (6.9).toFloat(), (4.6).toFloat(), (-4.7).toFloat(), (-8.9).toFloat(), (1.4).toFloat(), (-4.9).toFloat(), (2.4).toFloat(), (-6.7).toFloat(), (0.7).toFloat(), (-5.6).toFloat(), (5.0).toFloat(), (-1.9).toFloat(), (6.5).toFloat(), (-8.5).toFloat(), (-7.4).toFloat(), (4.8).toFloat(), (0.8).toFloat(), (-6.0).toFloat(), (-5.0).toFloat(), (-4.7).toFloat(), (2.9).toFloat(), (9.3).toFloat(), (-7.3).toFloat(), (-7.3).toFloat(), (4.4).toFloat(), (-4.6).toFloat(), (9.8).toFloat(), (-3.1).toFloat(), (8.2).toFloat(), (-6.5).toFloat(), (2.1).toFloat(), (-2.4).toFloat(), (-5.8).toFloat(), (-4.0).toFloat(), (9.0).toFloat(), (-3.7).toFloat(), (2.8).toFloat(), (8.2).toFloat(), (8.1).toFloat(), (3.1).toFloat(), (6.2).toFloat(), (-2.9).toFloat(), (1.6).toFloat(), (2.7).toFloat(), (0.3).toFloat(), (-0.6).toFloat(), (-4.1).toFloat(), (-9.0).toFloat(), (-9.8).toFloat(), (3.2).toFloat(), (-4.8).toFloat(), (-4.6).toFloat(), (-5.5).toFloat(), (-6.3).toFloat(), (0.2).toFloat(), (0.4).toFloat(), (7.0).toFloat(), (7.0).toFloat(), (6.1).toFloat(), (10.0).toFloat(), (8.8).toFloat(), (9.0).toFloat(), (8.9).toFloat(), (1.8).toFloat(), (2.2).toFloat(), (0.1).toFloat(), (9.7).toFloat(), (6.5).toFloat(), (9.4).toFloat(), (8.7).toFloat(), (-8.9).toFloat(), (6.4).toFloat(), (0.6).toFloat(), (-2.5).toFloat(), (-8.2).toFloat(), (-8.2).toFloat(), (2.8).toFloat(), (-2.9).toFloat(), (7.7).toFloat(), (6.3).toFloat(), (4.7).toFloat(), (9.1).toFloat(), (-7.1).toFloat(), (-1.5).toFloat(), (-1.2).toFloat(), (-5.3).toFloat(), (-3.5).toFloat(), (-8.1).toFloat(), (-5.2).toFloat(), (-6.6).toFloat(), (5.4).toFloat(), (-3.9).toFloat(), (-1.3).toFloat(), (0.9).toFloat(), (-8.6).toFloat(), (5.0).toFloat(), (-1.4).toFloat(), (-7.9).toFloat(), (-7.6).toFloat(), (-3.7).toFloat(), (-6.2).toFloat(), (10.0).toFloat(), (9.8).toFloat(), (3.1).toFloat(), (-8.6).toFloat(), (1.2).toFloat(), (-1.5).toFloat(), (3.8).toFloat(), (1.0).toFloat(), (-6.2).toFloat(), (-9.2).toFloat(), (-7.1).toFloat(), (3.0).toFloat(), (-7.9).toFloat(), (-8.5).toFloat(), (-3.4).toFloat(), (-1.6).toFloat(), (2.7).toFloat(), (-6.6).toFloat(), (3.9).toFloat(), (-3.6).toFloat(), (-2.6).toFloat(), (4.2).toFloat(), (-4.1).toFloat(), (-0.0).toFloat(), (-6.6).toFloat(), (-6.1).toFloat(), (6.3).toFloat(), (-0.4).toFloat(), (-3.0).toFloat(), (9.5).toFloat(), (8.3).toFloat(), (-4.5).toFloat(), (7.1).toFloat(), (4.8).toFloat(), (-3.5).toFloat(), (8.2).toFloat(), (9.6).toFloat(), (-3.6).toFloat(), (-3.2).toFloat(), (-5.8).toFloat(), (-6.3).toFloat(), (-0.7).toFloat(), (1.8).toFloat(), (0.1).toFloat(), (9.6).toFloat(), (-1.2).toFloat(), (0.8).toFloat(), (-5.3).toFloat(), (8.0).toFloat(), (7.4).toFloat(), (-3.4).toFloat(), (-2.3).toFloat(), (7.5).toFloat(), (8.8).toFloat(), (-6.5).toFloat(), (5.3).toFloat(), (6.9).toFloat(), (-9.2).toFloat(), (-8.1).toFloat(), (-7.5).toFloat(), (7.9).toFloat(), (3.7).toFloat(), (5.8).toFloat(), (-3.5).toFloat(), (-3.8).toFloat(), (7.7).toFloat(), (-5.2).toFloat(), (2.2).toFloat(), (4.5).toFloat(), (-7.0).toFloat(), (-3.6).toFloat(), (7.3).toFloat(), (1.7).toFloat(), (-9.0).toFloat(), (8.4).toFloat(), (9.3).toFloat(), (0.1).toFloat(), (-4.0).toFloat(), (-9.3).toFloat(), (-4.6).toFloat(), (-3.4).toFloat(), (7.0).toFloat(), (8.5).toFloat(), (0.9).toFloat(), (3.6).toFloat(), (4.1).toFloat(), (-2.9).toFloat(), (3.0).toFloat(), (2.1).toFloat(), (3.4).toFloat(), (-6.6).toFloat(), (9.8).toFloat(), (-8.7).toFloat(), (2.8).toFloat(), (7.8).toFloat(), (-5.1).toFloat(), (-8.8).toFloat(), (-8.9).toFloat(), (5.8).toFloat(), (7.2).toFloat(), (7.4).toFloat(), (0.2).toFloat(), (7.3).toFloat(), (6.2).toFloat(), (0.9).toFloat(), (-7.7).toFloat(), (2.0).toFloat(), (4.3).toFloat(), (9.7).toFloat(), (-5.7).toFloat(), (1.4).toFloat(), (7.1).toFloat(), (-8.7).toFloat(), (1.4).toFloat(), (9.0).toFloat(), (8.0).toFloat(), (1.8).toFloat(), (3.2).toFloat(), (7.5).toFloat(), (4.1).toFloat(), (-5.7).toFloat(), (0.6).toFloat(), (-9.7).toFloat(), (6.7).toFloat(), (6.9).toFloat(), (-5.2).toFloat(), (1.7).toFloat(), (0.8).toFloat(), (-8.2).toFloat(), (-3.8).toFloat(), (1.5).toFloat(), (-7.6).toFloat(), (2.2).toFloat(), (-0.4).toFloat(), (1.6).toFloat(), (9.2).toFloat())
    private var signalClassifier = SignalClassifier(this)
    private var firebasePerformance = FirebasePerformance.getInstance()
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private val BLE_CONNECTION = 1
    val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
    val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"
    private var mDeviceAddress: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Setup view instances

        yesButton = findViewById(R.id.yes_button)
        predictedTextView = findViewById(R.id.predicted_text)

        // Setup YES button
        yesButton?.setOnClickListener {
            //   Firebase.analytics.logEvent("correct_inference", null)
            classifySignal()
            val intent = Intent(this, DeviceScanActivity::class.java)
            startActivityForResult(intent, BLE_CONNECTION)
        }
        setupSignalClassifier()
    }

    private fun setupSignalClassifier() {
        // Add these lines to create and start the trace
        val downloadTrace = firebasePerformance.newTrace("download_model")
        downloadTrace.start()
        downloadModel("epilepsy_network")
                // Add these lines to stop the trace on success
                .addOnSuccessListener {
                    downloadTrace.stop()
                }
    }


    private fun configureRemoteConfig() {
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    private fun downloadModel(modelName: String): Task<Void> {
        val remoteModel = FirebaseCustomRemoteModel.Builder(modelName).build()
        val firebaseModelManager = FirebaseModelManager.getInstance()
        return firebaseModelManager
                .isModelDownloaded(remoteModel)
                .continueWithTask { task ->
                    // Create update condition if model is already downloaded, otherwise create download
                    // condition.
                    val conditions = if (task.result != null && task.result == true) {
                        FirebaseModelDownloadConditions.Builder()
                                .requireWifi()
                                .build() // Update condition that requires wifi.
                    } else {
                        FirebaseModelDownloadConditions.Builder().build() // Download condition.
                    }
                    firebaseModelManager.download(remoteModel, conditions)
                }
                .addOnSuccessListener {
                    firebaseModelManager.getLatestModelFile(remoteModel)
                            .addOnCompleteListener {
                                val model = it.result
                                if (model == null) {
                                    showToast("Failed to get model file.")
                                } else {
                                    showToast("Downloaded remote model: $modelName")
                                    signalClassifier.initialize(model)
                                }
                            }
                }
                .addOnFailureListener {
                    showToast("Model download failed for $modelName, please check your connection.")
                }
    }

    override fun onDestroy() {
        signalClassifier.close()
        super.onDestroy()
    }

    private fun handout_results() {

        if ((signal != null) && (signalClassifier.isInitialized)) {
            // Add these lines to create and start the trace
            val classifyTrace = firebasePerformance.newTrace("classify")
            classifyTrace.start()
            signalClassifier
                    .classifyAsync(signal)
                    .addOnSuccessListener { resultText ->
                        // Add this line to stop the trace on success
                        classifyTrace.stop()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Error classifying drawing.")
                    }
        }
    }

    private fun classifySignal() {
        if ((signal != null) && (signalClassifier.isInitialized)) {
            // Add these lines to create and start the trace
            val classifyTrace = firebasePerformance.newTrace("classify")
            classifyTrace.start()
            signalClassifier
                    .classifyAsync(signal)
                    .addOnSuccessListener { resultText ->
                        // Add this line to stop the trace on success
                        predictedTextView?.text = resultText
                        classifyTrace.stop()
                    }
                    .addOnFailureListener { e ->
                        predictedTextView?.text = getString(
                                R.string.classification_error_message,
                                e.localizedMessage
                        )   //it's red but it compiles... Life is life ?
                        Log.e(TAG, "Error classifying drawing.", e)
                    }
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun showToast(text: String) {
        Toast.makeText(
                this,
                text,
                Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val MODEL_FILE = "epilepsy_network.tflite"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BLE_CONNECTION && resultCode == RESULT_OK) {
            if (data != null) {
                mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS)
            }
        }
    }
}
