package ch.epfl.seizuredetection.GUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ch.epfl.seizuredetection.R;

public class EditProfileActivity extends AppCompatActivity {

    public static final String USER_ID = "USER_ID";
    static EditText editTextEmail;
    static EditText editTextPassword;
    static EditText editHeight;
    static EditText editWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editTextEmail = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Pwd);
        editHeight = findViewById(R.id.Height);
        editWeight = findViewById(R.id.Weight);

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

        //Put values from user in EditTexts
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();


        if (user != null) {

            String email = user.getEmail();
            editTextEmail.setText(email);
            String uid = user.getUid();


                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("profiles").child(uid);
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                           String height = snapshot.child("height").getValue().toString();
                            editHeight.setText(height);
                            String weight = snapshot.child("weight").getValue().toString();
                            editWeight.setText(weight);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


        }

    }
}
