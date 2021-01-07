package ch.epfl.seizuredetection.GUI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import ch.epfl.seizuredetection.R;

public class EditProfileActivity extends AppCompatActivity {

    public static final String USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Toolbar action to go back to previous activity
        View backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        //Remove profile button from toolbar
        View profileButton = findViewById(R.id.profile);
        ViewGroup parent2 = (ViewGroup)profileButton.getParent();
        parent2.removeView(profileButton);
    }
}
