package ch.epfl.seizuredetection.GUI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import ch.epfl.seizuredetection.R;

public class EditProfileActivity extends AppCompatActivity {

    public static final String USER_ID = "USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
    }
}
