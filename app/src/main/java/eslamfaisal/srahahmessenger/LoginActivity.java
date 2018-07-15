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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @BindView(R.id.login_button)
     Button loginButton;

    @BindView(R.id.login_email)
     EditText loginEmail;

    @BindView(R.id.login_password)
    EditText loginPassword;

    ProgressDialog progressDialog;

    //firebase instances

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserReference = mFirebaseDatabase.getReference().child("users");
        mUserReference.keepSynced(true);

        progressDialog = new ProgressDialog(this);

        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = loginEmail.getText().toString().trim();
                String userPassword = loginPassword.getText().toString().trim();

                loginUserAccount(userEmail, userPassword);
            }
        });
    }

    private void loginUserAccount(String userEmail, String userPassword) {
        if (TextUtils.isEmpty(userEmail) | TextUtils.isEmpty(userPassword) | TextUtils.isEmpty(userPassword)) {
            Toast.makeText(LoginActivity.this, "please fill all fields", Toast.LENGTH_LONG).show();
        } else {

            progressDialog.setTitle("Log In");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String currentUserId = mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        mUserReference.child(currentUserId).child("device_token").setValue(deviceToken)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this,"Email Signed In",Toast.LENGTH_SHORT).show();
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        });


                    }else {
                        Toast.makeText(LoginActivity.this, "Wrong Email Or Password", Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                }
            });

        }
    }
}
