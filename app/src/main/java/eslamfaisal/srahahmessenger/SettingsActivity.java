package eslamfaisal.srahahmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    public static final String STATUS_STRING = "status_string";

    private CircleImageView profileImageView;
    private TextView userName;
    private TextView userStatus;
    private Button changProfileImage;
    private Button changeStatus;

    private Bitmap thumbBitmap = null;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserProfileDataReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference profileImageStorageReference;
    private StorageReference thumbImagesStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        String userId = mAuth.getCurrentUser().getUid();

        mUserProfileDataReference = mFirebaseDatabase.getReference().child("users").child(userId);
        mUserProfileDataReference.keepSynced(true);
        profileImageStorageReference = mFirebaseStorage.getReference().child("profile_images");
        thumbImagesStorageReference = mFirebaseStorage.getReference().child("thumb_images");

        profileImageView = findViewById(R.id.profile_image_in_settings_activity);
        userName = findViewById(R.id.user_name_in_settings_activity);
        userStatus = findViewById(R.id.user_status_in_settings_activity);
        changeStatus = findViewById(R.id.change_status_button_in_settings_activity);
        changProfileImage = findViewById(R.id.change_image_button_in_settings_activity);

        mUserProfileDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String profileImageUri = dataSnapshot.child("user_thumb_image").getValue().toString();

                if (!(profileImageUri.equals("default_image"))) {

                    Picasso.get().load(profileImageUri).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(profileImageView,
                            new Callback() {
                                @Override
                                public void onSuccess() {}

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(profileImageUri).placeholder(R.drawable.profile).into(profileImageView);
                                }
                            });

                }
                userName.setText(name);
                userStatus.setText(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldStatus = userStatus.getText().toString();

                Intent intent = new Intent(SettingsActivity.this, StatusActivity.class);
                intent.putExtra(STATUS_STRING, oldStatus);
                startActivity(intent);
            }
        });

        changProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                showProgress();
                Uri resultUri = result.getUri();

                bitmapCompress(resultUri);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumbByte = byteArrayOutputStream.toByteArray();

                final String uid = mAuth.getCurrentUser().getUid();
                final StorageReference originalFilePathRef = profileImageStorageReference.child(uid + ".jpg");
                final StorageReference thumbFilePathRef = thumbImagesStorageReference.child(uid + ".jpg");

                uploadOriginalImage(resultUri, originalFilePathRef);
                uploadThumbImage(thumbByte, thumbFilePathRef);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void showProgress() {
        progressDialog.setTitle("Update Friend Image");
        progressDialog.setMessage("Pleas Wait");
        progressDialog.show();
    }

    private void bitmapCompress(Uri resultUri) {
        final File thumbFilepathUri = new File(resultUri.getPath());

        try {
            thumbBitmap = new Compressor(this)
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(50)
                    .compressToBitmap(thumbFilepathUri);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //upload thumb image
    private void uploadThumbImage(byte[] thumbByte, final StorageReference thumbFilePathRef) {

        thumbFilePathRef.putBytes(thumbByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                thumbFilePathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri thumbUri) {
                        mUserProfileDataReference.child("user_thumb_image").setValue(thumbUri.toString());
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    //upload original image
    private void uploadOriginalImage(Uri resultUri, final StorageReference filePath) {

        filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri imageUri) {
                        mUserProfileDataReference.child("user_image").setValue(imageUri.toString());
                    }
                });
            }
        });
    }
}
