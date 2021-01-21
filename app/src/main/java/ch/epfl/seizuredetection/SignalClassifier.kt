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

@file:Suppress("DEPRECATION")

package ch.epfl.seizuredetection


import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.call
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt

class SignalClassifier(private val context: Context) {
  private var interpreter: Interpreter? = null
  var isInitialized = false
    private set

  var signalLen = 741

  /** Executor to run inference task in the background */
  private val executorService: ExecutorService = Executors.newCachedThreadPool()

  private var modelInputSize: Int = 0 // will be inferred from TF Lite model

  fun initialize(model: Any): Task<Void> {
    return call(
            executorService,
            Callable<Void> {
              initializeInterpreter(model)
              null
            }
    )
  }

  private fun initializeInterpreter(model: Any) {
    // Initialize TF Lite Interpreter with NNAPI enabled
    val options = Interpreter.Options()
    options.setUseNNAPI(true)
    var interpreter: Interpreter
    if (model is ByteBuffer) {
      interpreter = Interpreter(model, options)
    } else {
      interpreter = Interpreter(model as File, options)
    }
    // Read input shape from model file
    val inputShape = interpreter.getInputTensor(0).shape()

    // CHANGE THIS ASAP
    modelInputSize = 3072

    // Finish interpreter initialization
    this.interpreter = interpreter
    isInitialized = true
    Log.d(TAG, "Initialized TFLite interpreter.")
  }

  private fun classify(sig: FloatArray): String {
    if (!isInitialized) {
      throw IllegalStateException("TF Lite Interpreter is not initialized yet.")
    }

    var startTime: Long
    var elapsedTime: Long

    // Preprocessing: resize the input
    startTime = System.nanoTime()
    val byteBuffer = convertArrayToByteBuffer(sig)
    elapsedTime = (System.nanoTime() - startTime) / 1000000
    Log.d(TAG, "Preprocessing time = " + elapsedTime + "ms")

    startTime = System.nanoTime()
    val result = Array(1) { Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) } } // use inputShape ??
    interpreter?.run(byteBuffer, result)
    elapsedTime = (System.nanoTime() - startTime) / 1000000
    Log.d(TAG, "Inference time = " + elapsedTime + "ms")

    return getOutputString(result[0][0])
  }

  fun classifyAsync(bitmap: FloatArray): Task<String> {
    return call(executorService, Callable<String> { classify(bitmap) })
  }

  fun close() {
    call(
            executorService,
            Callable<String> {
              interpreter?.close()
              Log.d(TAG, "Closed TFLite interpreter.")
              null
            }
    )
  }

  private fun convertArrayToByteBuffer(sig: FloatArray): ByteBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
    byteBuffer.order(ByteOrder.nativeOrder())

    var filtered_sig = butterworthLowPassFilter(sig)

    val mean = filtered_sig.sum() / signalLen
    var std = 0.0
    for (i in filtered_sig.indices) {
      filtered_sig[i] -= mean
      std += filtered_sig[i] * filtered_sig[i] / signalLen
    }
    std = sqrt(std)
    for (i in filtered_sig.indices) {
      filtered_sig[i] = (filtered_sig[i] / std).toFloat()
      byteBuffer.putFloat(filtered_sig[i])
    }
    return byteBuffer
  }

  private fun butterworthLowPassFilter(sig: FloatArray): FloatArray {
    val output = FloatArray(signalLen)
    // Tuned parameters
    var alpha = 0.1735
    output[0] = sig[0]
    for (i in 1..signalLen - 1) {
      output[i] = (sig[i - 1] + sig[i] + alpha * output[i - 1]).toFloat()
    }
    return output
  }

  private fun getOutputString(output: FloatArray): String {
    val maxIndex = output.indices.maxBy { output[it] } ?: -1
    return "Prediction Result: %d\nConfidence: %2f".format(maxIndex, output[maxIndex])
  }

  companion object {
    private const val TAG = "DigitClassifier"

    private const val FLOAT_TYPE_SIZE = 4

    private const val OUTPUT_CLASSES_COUNT = 2
    private const val MODEL_NAME_KEY = "model_name"
  }
}
