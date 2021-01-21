package ch.epfl.seizuredetection.GUI;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import ch.epfl.seizuredetection.R;

public class ResultsActivity extends AppCompatActivity {

    private Interpreter interpreter;
    private float probabilityToDie = -1;
    private String TAG = "resultsActivity";
    private TextView mStrokeResults;
    private Button mAccept;
    byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        mStrokeResults = findViewById(R.id.strokeResults);
        mAccept = findViewById(R.id.AcceptButton);
        mAccept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(ResultsActivity.this, "Accept your heart like it is", Toast.LENGTH_SHORT).show();
                Toast.makeText(ResultsActivity.this, "Don't eat too much cholesterol", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
                startActivity(intent);
            }});

        //Remove going back button from toolbar
        View backButton = findViewById(R.id.backButton);
        ViewGroup parent = (ViewGroup)backButton.getParent();
        parent.removeView(backButton);

        //Remove profile button from toolbar
        View profileButton = findViewById(R.id.profile);
        ViewGroup parent2 = (ViewGroup)profileButton.getParent();
        parent2.removeView(profileButton);

        // Get the Intent that started this activity and extract the string
        Bundle bundle = getIntent().getExtras();
        float[] ecg = bundle.getFloatArray(LiveActivity.SIGNAL);
        byteArray = floatArrayToByteArray(ecg);
        probabilityToDie = analyseNN0(byteArray);
        if(probabilityToDie > 0){
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:144"));
                startActivity(callIntent);
            } catch (ActivityNotFoundException activityException) {
                Log.e("Calling a Phone Number", "Call failed", activityException);
            }
        }
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
        Toast.makeText(ResultsActivity.this, "Signal was analysed", Toast.LENGTH_SHORT).show();

        return probabilities.get(0);
    }


    private void amIDead() {

        // trigger activity to call ambulance
    }

}