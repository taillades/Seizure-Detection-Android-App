package ch.epfl.seizuredetection.GUI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import ch.epfl.seizuredetection.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }
}