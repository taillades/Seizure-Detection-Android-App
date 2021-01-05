package ch.epfl.seizuredetection.GUI;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import ch.epfl.seizuredetection.POJO.Profile;
import ch.epfl.seizuredetection.R;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Auth database instance
    private final String TAG = this.getClass().getSimpleName();
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextWeight;
    private EditText editTextHeight;
    public static final String USERNAME = "poupoupou";
    public static final String PASSWORD = "pouloulou";
    TextView textLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance(); // Auth database instance
        editTextEmail = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Pwd);
        editTextWeight = findViewById(R.id.Weight);
        editTextHeight = findViewById(R.id.Height);
        textLogin = findViewById(R.id.textLogin);

        String text = "Already registered? Login";
        SpannableString signclick = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {

                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);

            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
            }
        };

        signclick.setSpan(clickableSpan,20,25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textLogin.setText(signclick);
        textLogin.setMovementMethod(LinkMovementMethod.getInstance());

        Button btnJoin = findViewById(R.id.Join);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String height = editTextHeight.getText().toString().trim();
                String weight = editTextWeight.getText().toString().trim();

                if(email.isEmpty()){
                    editTextEmail.setError("Email is required");
                    editTextEmail.requestFocus();
                }
                else if (password.isEmpty()){
                    editTextPassword.setError("Password is required!");
                    editTextPassword.requestFocus();
                }
                else if (password.length() < 6){
                    editTextPassword.setError("The password must be at least 6 characters long");
                    editTextPassword.requestFocus();
                }
                else if (height.isEmpty()){
                    editTextHeight.setError("Height is required!");
                    editTextHeight.requestFocus();
                }
                else if (weight.isEmpty()){
                    editTextWeight.setError("Weight is required!");
                    editTextWeight.requestFocus();
                }   else
                    registerUser(email, password, height, weight);
            }
        });
}

    private void registerUser(final String email, final String password, final String height, final String weight) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Profile user = new Profile(email, password, Integer. parseInt(height), Integer. parseInt(weight));
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            // Adding the profile to the database
                            FirebaseDatabase.getInstance().getReference("profiles").child(currentUser.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "User has been registered successfuly", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "User has not been registered successfuly", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra(USERNAME, user.getUsername());
                            intent.putExtra(PASSWORD, user.getPassword());
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }
}
