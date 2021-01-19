package ch.epfl.seizuredetection.GUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelInputs;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import ch.epfl.seizuredetection.R;

public class ResultsActivity extends AppCompatActivity {

    private Interpreter interpreter;
    private float probabilityToDie = 0;
    private String TAG = "resultsActivity";
    private TextView mStrokeResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        mStrokeResults = findViewById(R.id.strokeResults);

        // Get the Intent that started this activity and extract the string
        Bundle bundle = getIntent().getExtras();
        float[] ecg = bundle.getFloatArray(LiveActivity.SIGNAL);
        byte[] byteArray = floatArrayToByteArray(ecg);
        probabilityToDie = analyseNN0(byteArray);
        mStrokeResults.setText(String.valueOf(probabilityToDie));
    }

    private static byte[] floatArrayToByteArray(float[] input)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES * input.length);
        for(int i=1; i<input.length;i++){
            buffer.putFloat(input[i]);
        }
        return buffer.array();
    }

    private float analyseNN0(byte[] byteBufferIn){
        FirebaseCustomRemoteModel remoteModel =
                new FirebaseCustomRemoteModel.Builder("epilepsy_network").build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Toast.makeText(ResultsActivity.this, "Analysis model downloaded", Toast.LENGTH_SHORT).show();
                    }
                });

        remoteModel = new FirebaseCustomRemoteModel.Builder("epilepsy_network").build();
        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
                .addOnCompleteListener(new OnCompleteListener<File>() {

                    @Override
                    public void onComplete(@NonNull Task<File> task) {
                        File modelFile = task.getResult();
                        if (modelFile != null) {
                            interpreter = new Interpreter(modelFile);
                        }
                    }
                });


        int bufferSize = 10 * Float.SIZE / Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        try{
            interpreter.run(byteBufferIn, modelOutput);
        }catch(Exception e){
            Log.e(TAG, e.getStackTrace().toString());
        }


        // Sets the probability to have a seizure
        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        return probabilities.get(0);
    }


    private void amIDead() {

        // trigger activity to call ambulance
    }

}