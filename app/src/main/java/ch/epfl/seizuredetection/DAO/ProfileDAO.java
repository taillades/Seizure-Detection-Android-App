package ch.epfl.seizuredetection.DAO;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import ch.epfl.seizuredetection.GUI.EditProfileActivity;
import ch.epfl.seizuredetection.POJO.Profile;
import ch.epfl.seizuredetection.R;

public class ProfileDAO{

    // Checks if the username and password correspond to a valid user
    // TODO: change it to authentication with Firebase
    public static String login(final String username, final String password){

        // Gets the Firebase instance
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileRef = database.getReference("profiles");
        final String[] userID = new String[1];

        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot user : dataSnapshot.getChildren()) {
                    String usernameDatabase = user.child("username")
                            .getValue(String.class);
                    String passwordDatabase = user.child("password")
                            .getValue(String.class);
                    if (username.equals(usernameDatabase)
                            && password.equals(passwordDatabase)) {
                        userID[0] = user.getKey();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return userID[0];
    }

    // Gets user given a userID
    public static Profile getProfile(String userID){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileRef = database.getReference("profiles");
        final Profile profile = new Profile();

        profileRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profile.setUsername(dataSnapshot.child("username").getValue(String.class));
                profile.setPassword(dataSnapshot.child("password").getValue(String.class));
                profile.setHeight(dataSnapshot.child("height").getValue(int.class));
                profile.setWeight(dataSnapshot.child("weight").getValue(float.class));
                profile.setPhotoPath(dataSnapshot.child("photo").getValue(String.class));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Empty
            }
        });
        return profile;
    }

    public static boolean register(Profile profile, Bitmap bitmap){

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileRef = database.getReference("profiles");
        final boolean[] registerOk = {true};

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference photoRef = storageRef.child("photos").child(profileRef.getKey() + ".jpg");
        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                registerOk[0] = false;
            }
        }).addOnSuccessListener(new PhotoUploadSuccessListener(profile, profileRef));
        return registerOk[0];
    }

/*    public static boolean editProfile(Profile profile){

    }*/

    private static Bitmap getPhotoBitmap(String photo){
        final Bitmap[] selectedImage = new Bitmap[0];
        //  Reference to an image file in Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl
                (photo);
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                selectedImage[0] = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length);
            }
        });

        return selectedImage[0];
    }

    private static class PhotoUploadSuccessListener implements
            OnSuccessListener<UploadTask.TaskSnapshot> {
        private Profile userProfile;
        private DatabaseReference profileRef;

        PhotoUploadSuccessListener(Profile userProfile, DatabaseReference profileRef){

        }

        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            taskSnapshot.getMetadata()
                    .getReference()
                    .getDownloadUrl()
                    .addOnSuccessListener(
                            new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    userProfile.setPhotoPath(uri.toString());
                                    profileRef.runTransaction(new ProfileDataUploadHandler(userProfile));
                                }
                            });
        }
    }

    private static class ProfileDataUploadHandler implements Transaction.Handler {
        private Profile userProfile;


        public ProfileDataUploadHandler(Profile userProfile){
            this.setUserProfile(userProfile);

        }

        public Profile getUserProfile() {
            return userProfile;
        }

        public void setUserProfile(Profile userProfile) {
            this.userProfile = userProfile;
        }


        @NonNull
        @Override
        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
            // TODO : cambiar para q no use userProfile
            mutableData.child("username").setValue(userProfile.getUsername());
            mutableData.child("password").setValue(userProfile.getPassword());
            mutableData.child("height").setValue(userProfile.getHeight());
            mutableData.child("weight").setValue(userProfile.getWeight());
            mutableData.child("photo").setValue(userProfile.getPhotoPath());
            return Transaction.success(mutableData);
        }

        @Override
        public void onComplete(@Nullable DatabaseError databaseError, boolean success, @Nullable
                DataSnapshot dataSnapshot) {
            if (success) {
                // TODO: send ok or something
                // TODO: update the profile some way ?
             /*   Intent intent = new Intent();
                intent.putExtra(MyProfileFragment.USER_PROFILE, userProfile);
                setResult(AppCompatActivity.RESULT_OK, intent);
                finish();*/
            } else {
                // TODO: send error or smth
            }
        }
    }

}