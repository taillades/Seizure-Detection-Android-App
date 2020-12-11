package ch.epfl.seizuredetection.GUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ch.epfl.seizuredetection.R;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth; // Auth database instance
    private final String TAG = this.getClass().getSimpleName();
    EditText editTextEmail;
    EditText editTextPassword;
    Button LoginButton;
    String ID;
    TextView SignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance(); // Auth database instance
        editTextEmail = findViewById(R.id.LoginEmail);
        editTextPassword = findViewById(R.id.LoginPwd);
        LoginButton = findViewById(R.id.LoginButton);
        SignUp = findViewById(R.id.SignUp);

        String text = "Don't have an account? Sign up";
        SpannableString signclick = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {

                //Intent intent = new Intent(getActivity(), RegisterActivity.class);
                //(() getActivity()).startActivity(intent);

            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
            }
        };

        signclick.setSpan(clickableSpan,23,30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SignUp.setText(signclick);
        SignUp.setMovementMethod(LinkMovementMethod.getInstance());

        LoginButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String pwd = editTextPassword.getText().toString();
                login(email, pwd);

            }
        });
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
                            Toast.makeText(LoginActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            editTextPassword.setError("Wrong email or password");
                            editTextPassword.requestFocus();
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}