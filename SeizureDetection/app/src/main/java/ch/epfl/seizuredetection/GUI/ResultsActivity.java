package ch.epfl.seizuredetection.GUI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import ch.epfl.seizuredetection.R;

public class ResultsActivity extends AppCompatActivity {

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
}
