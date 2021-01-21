package ch.epfl.seizuredetection.GUI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

import ch.epfl.seizuredetection.Data.AppDatabase;
import ch.epfl.seizuredetection.Data.Constant;
import ch.epfl.seizuredetection.Data.ProfileEntity;
import ch.epfl.seizuredetection.R;

public class EditProfileActivity extends AppCompatActivity {

    public static String USER_ID = "USER_ID";
    EditText editTextEmail;
    EditText editTextPassword;
    EditText editHeight;
    EditText editWeight;
    EditText editRepeatPwd;
    private static final String TAG = "EditProfileActivity";

    //Firebase
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseUser user = auth.getCurrentUser();
    private static final String uid = user.getUid();
    private static final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("profiles").child(uid);

    //Edit profile Firebase part
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference profileGetRef = database.getReference("profiles").child(uid);
    private static DatabaseReference profileRef = profileGetRef.push();

    //current values
    public String email;
    public int height;
    public float weight;
    public String pwd;
    public Button changesButton;

    //new values
    public String newEmail;
    public String newHeight;
    public String newWeight;
    public String newPwd;
    public String repeatPwd;

    //SQLite
    AppDatabase db;
    String idSQ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editTextEmail = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Pwd);
        editHeight = findViewById(R.id.Height);
        editWeight = findViewById(R.id.Weight);
        editRepeatPwd = findViewById(R.id.RepeatPwd);

        //Get USER_ID extra from main for SQLite
        Intent intent = getIntent();
        if (intent != null) {
             idSQ = intent.getStringExtra("USER_ID");
           // Toast.makeText(EditProfileActivity.this, "valor columna" + idSQ, Toast.LENGTH_SHORT).show();
        }
        db = Room.databaseBuilder(getApplicationContext(),AppDatabase.class, Constant.BD_NAME)
                .allowMainThreadQueries()
                .build();


        //Toolbar action to go back to previous activity
        View backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        //Remove profile button from toolbar
        View profileButton = findViewById(R.id.profile);
        ViewGroup parent2 = (ViewGroup) profileButton.getParent();
        parent2.removeView(profileButton);

        //Read value from firebase and write them in EditTexts
        readProfileValues();

        //See if textviews change to know if user wants to change any parameter
        editTextEmail.addTextChangedListener(editTextWatcher);
        editTextPassword.addTextChangedListener(editTextWatcher);
        editHeight.addTextChangedListener(editTextWatcher);
        editWeight.addTextChangedListener(editTextWatcher);
        editRepeatPwd.addTextChangedListener(editTextWatcher);

        //When button is clicked change parameters in firebase if needed
        changesButton = findViewById(R.id.SaveChanges);
        changesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
    }

    private void readProfileValues() {
        if (user != null) {
            //E-mail
            email = user.getEmail();
            editTextEmail.setText(email);

            //Height and Weight
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    height = snapshot.child("height").getValue(int.class);
                    weight = snapshot.child("weight").getValue(float.class);
                    pwd = snapshot.child("password").getValue().toString();
                    editTextPassword.setText(pwd);
                    editHeight.setText(String.valueOf(height));
                    editWeight.setText(String.valueOf(weight));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Failed to read values from Firebase, use SQLite
                    //Log.w(TAG, , error.toException());
                    Toast.makeText(EditProfileActivity.this, "Failed to read values from firebase.", Toast.LENGTH_SHORT).show();
                    height = db.profileDAO().getHeight(Integer. parseInt(idSQ));
                    weight = db.profileDAO().getWeight(Integer. parseInt(idSQ));
                    pwd  = db.profileDAO().getPassword(Integer. parseInt(idSQ));

                    editTextPassword.setText(pwd);
                    editHeight.setText(String.valueOf(height));
                    editWeight.setText(String.valueOf(weight));
                    Toast.makeText(EditProfileActivity.this, pwd, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private TextWatcher editTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Take new values
            newEmail = editTextEmail.getText().toString().trim(); //.trim() empty removes spaces
            newPwd = editTextPassword.getText().toString().trim();
            newHeight = editHeight.getText().toString().trim();
            newWeight = editWeight.getText().toString().trim();
            repeatPwd = editRepeatPwd.getText().toString().trim();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void updateProfile() {
        if (newWeight.isEmpty()) {
            editWeight.setError("New weight can not be empty");
            editWeight.requestFocus();
        } else if (newEmail.isEmpty()) {
            editTextEmail.setError("New E-mail can not be empty");
            editTextEmail.requestFocus();
        }else if (newPwd.isEmpty()){
            editTextPassword.setError("New password can not be empty");
            editTextPassword.requestFocus();
        }else if (newHeight.isEmpty()) {
            editHeight.setError("New height can not be empty");
            editHeight.requestFocus();
        } else if (newPwd.length() < 6) {
            editTextPassword.setError("New password must be at least 6 characters long");
            editTextPassword.requestFocus();
        } else if (!newPwd.equals(pwd) && !newPwd.equals(repeatPwd)) {
            editRepeatPwd.setError("New password does not match");
            editRepeatPwd.requestFocus();
        }else if(newEmail.equals(email) && newWeight.equals(weight) && newHeight.equals(height) && newPwd.equals(pwd)){
            Toast.makeText(EditProfileActivity.this, "No values changed", Toast.LENGTH_SHORT).show();
        }else {
            profileGetRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    currentData.child("height").setValue(Integer.valueOf(newHeight));
                    currentData.child("weight").setValue(Float.valueOf(newWeight));
                    currentData.child("username").setValue(newEmail);
                    currentData.child("password").setValue(newPwd);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (committed) {
                        Toast.makeText(EditProfileActivity.this, R.string.registration_success, Toast
                                .LENGTH_SHORT).show();

                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, R.string.registration_failed, Toast
                                .LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
