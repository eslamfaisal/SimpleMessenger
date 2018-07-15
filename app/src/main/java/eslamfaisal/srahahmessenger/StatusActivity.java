package eslamfaisal.srahahmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button saveChangesButton;
    private EditText changeStatusEditText;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mStatusReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        String userId = mAuth.getCurrentUser().getUid();

        mStatusReference = mFirebaseDatabase.getReference().child("users").child(userId);


        mToolbar = findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chang Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        changeStatusEditText = findViewById(R.id.chang_status_edittext);

        String oldStatus = getIntent().getExtras().get(SettingsActivity.STATUS_STRING).toString();
        if (!TextUtils.isEmpty(oldStatus)) {
            if (oldStatus.length() > 0) {
                changeStatusEditText.setText(oldStatus);
            }
        }

        saveChangesButton = findViewById(R.id.save_change_status_button);
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = changeStatusEditText.getText().toString().trim();
                changeStatus(newStatus);
            }
        });
    }

    private void changeStatus(String newStatus) {
        if (TextUtils.isEmpty(newStatus)) {
            Toast.makeText(StatusActivity.this, "Pleas Write Your Zeft", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Change Status");
            progressDialog.setMessage("Pleas Wait");
            progressDialog.show();
            mStatusReference.child("user_status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        finish();
                        Toast.makeText(StatusActivity.this, "Status Changed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(StatusActivity.this, "Error pleas tray again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
