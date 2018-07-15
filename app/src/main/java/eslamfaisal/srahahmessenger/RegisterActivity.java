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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText registerUserName;
    private EditText registerUserEmail;
    private EditText registerUserPassword;
    private Button createAccountButton;

    private ProgressDialog progressDialog;

    //firebase instances
    private FirebaseAuth mAuth ;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference userDataReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();



        progressDialog = new ProgressDialog(this);

        registerUserName = findViewById(R.id.resgister_name);
        registerUserEmail = findViewById(R.id.register_email);
        registerUserPassword = findViewById(R.id.register_password);
        createAccountButton = findViewById(R.id.create_account_button);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = registerUserName.getText().toString().trim();
                String userEmail = registerUserEmail.getText().toString().trim();
                String userPassword = registerUserPassword.getText().toString().trim();

                registerAccount(userName, userEmail, userPassword);
            }
        });
    }

    private void registerAccount(final String userName, String userEmail, String userPassword) {
        if (TextUtils.isEmpty(userName)|TextUtils.isEmpty(userEmail)|TextUtils.isEmpty(userPassword)){
            Toast.makeText(RegisterActivity.this,"please fill all fields",Toast.LENGTH_LONG).show();
        }else {
            progressDialog.setTitle("Create New Account");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        String currentUserId = mAuth.getUid();
                        userDataReference = mFirebaseDatabase.getReference().child("users").child(currentUserId);
                        userDataReference.child("user_name").setValue(userName);
                        userDataReference.child("device_token").setValue(deviceToken);
                        userDataReference.child("user_status").setValue("Hey I'am Here");
                        userDataReference.child("user_image").setValue("default_profile");
                        userDataReference.child("online").setValue(true);
                        userDataReference.child("user_thumb_image").setValue("default_image").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(RegisterActivity.this, "Email Registered", Toast.LENGTH_SHORT).show();
                                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }
                        });

                    }else {
                        Toast.makeText(RegisterActivity.this,"Error pleas try again",Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            });
        }
    }
}
