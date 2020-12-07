package ch.epfl.seizuredetection.GUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ch.epfl.seizuredetection.DAO.ProfileDAO;
import ch.epfl.seizuredetection.R;

public class LoginActivity extends AppCompatActivity {
    EditText LoginEmail;
    EditText LoginPwd;
    Button LoginButton;
    String ID;
    TextView SignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginEmail = findViewById(R.id.LoginEmail);
        LoginPwd = findViewById(R.id.LoginPwd);
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
                String email = LoginEmail.getText().toString();
                String pwd = LoginPwd.getText().toString();

                ID = ProfileDAO.login(email,pwd);
            }
        });
     }

}
