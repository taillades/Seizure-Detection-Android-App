package ch.epfl.seizuredetection.GUI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;

import ch.epfl.seizuredetection.Data.AppDatabase;
import ch.epfl.seizuredetection.Data.Constant;
import ch.epfl.seizuredetection.Data.ProfileEntity;
import ch.epfl.seizuredetection.POJO.Profile;
import ch.epfl.seizuredetection.R;
import ch.epfl.seizuredetection.ml.CompressionNn0;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth; // Auth database instance
    private final String TAG = this.getClass().getSimpleName();
    static EditText editTextEmail;
    static EditText editTextPassword;
    Button LoginButton;
    String ID;
    TextView SignUp;
    AppDatabase db;

    public static void updateUI(Profile user) {
        editTextEmail.setText(user.getUsername());
        editTextPassword.setText(user.getPassword());
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra(EditProfileActivity.USER_ID, user.getUid());
                            int id = db.profileDAO().getId(email);
                            intent.putExtra("USER_ID_SQ",Integer. toString(id));
                            Toast.makeText(LoginActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        } else {
                            //If authentication with Firebase fails, try with local storage
                            //Toast.makeText(LoginActivity.this, "Authentication with Firebase failed.", Toast.LENGTH_SHORT).show();
                            loginSQL(email,password);
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Remove going back button from toolbar
        View backButton = findViewById(R.id.backButton);
        ViewGroup parent = (ViewGroup) backButton.getParent();
        parent.removeView(backButton);

        //Remove profile button from toolbar
        View profileButton = findViewById(R.id.profile);
        ViewGroup parent2 = (ViewGroup) profileButton.getParent();
        parent2.removeView(profileButton);

        //Call SQLite db
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, Constant.BD_NAME)
                .allowMainThreadQueries()
                .build();

        mAuth = FirebaseAuth.getInstance(); // Auth database instance
        editTextEmail = findViewById(R.id.LoginEmail);
        editTextPassword = findViewById(R.id.LoginPwd);
        LoginButton = findViewById(R.id.LoginButton);
        SignUp = findViewById(R.id.SignUp);

        Intent intent = getIntent();
        if (intent != null) {
            editTextEmail.setText(intent.getStringExtra(RegisterActivity.USERNAME));
            editTextPassword.setText(intent.getStringExtra(RegisterActivity.PASSWORD));
        }

        String text = "Don't have an account? Sign up";
        SpannableString signclick = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
            }
        };

        signclick.setSpan(clickableSpan, 23, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SignUp.setText(signclick);
        SignUp.setMovementMethod(LinkMovementMethod.getInstance());

        LoginButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String pwd = editTextPassword.getText().toString();

                //Firebase
                login(email, pwd);

            }
        });
    }

    private void loginSQL(String email, String password) {
        String pwd = db.profileDAO().getPwd(email);
        if (pwd != null && pwd.equals(password)) {
            int id = db.profileDAO().getId(email);
            Toast.makeText(LoginActivity.this, "Seizure detection is not available without internet connection.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("USER_ID", Integer.toString(id));
            startActivity(intent);
        } else {
            editTextPassword.setError("Wrong email or password");
            editTextPassword.requestFocus();
        }
    }
}