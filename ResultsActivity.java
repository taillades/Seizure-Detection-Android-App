package ch.epfl.seizuredetection.GUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import ch.epfl.seizuredetection.R;

public class ResultsActivity extends AppCompatActivity {

    private Interpreter interpreter;
    private float probabilityToDie = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        //Remove going back button from toolbar
        View backButton = findViewById(R.id.backButton);
        ViewGroup parent = (ViewGroup)backButton.getParent();
        parent.removeView(backButton);

        //Toolbar action to Edit Profile Activity
        View profileButton = findViewById(R.id.profile);
        profileButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(ResultsActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });
    }


    private static byte[] floatArrayToByteArray(float[] input)
    {
        final ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES * input.length);
        buffer.asFloatBuffer().put(input);
        return buffer.array();
    }

    private void analyseNN0(TensorBuffer byteBufferIn){
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
        interpreter.run(byteBufferIn, modelOutput);

        // Sets the probability to have a seizure
        modelOutput.rewind();
        FloatBuffer probabilities = modelOutput.asFloatBuffer();
        probabilityToDie = probabilities.get(0);
    }


    private void amIDead() {

        // trigger activity to call ambulance
    }

}
