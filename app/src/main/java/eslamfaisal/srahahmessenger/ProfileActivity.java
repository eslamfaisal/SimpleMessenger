package eslamfaisal.srahahmessenger;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import eslamfaisal.srahahmessenger.modules.User;

public class ProfileActivity extends AppCompatActivity {

    public static final String CLICKED_USER_ID = "id";

    @BindView(R.id.profile_visit_user_name)
    TextView profileName;

    @BindView(R.id.profile_visit_user_status)
    TextView profileStatus;

    @BindView(R.id.profile_visit_user_image)
    ImageView profileImage;

    @BindView(R.id.send_request_button)
    Button sendRequestButton;

    @BindView(R.id.decline_request_button)
    Button declineRequestButton;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFriendsReference;
    private DatabaseReference mUserProfileReference;
    private DatabaseReference mFriendRequestReference;
    private DatabaseReference mNotificationReference;

    private String sender_user_id;
    private String receiver_user_id;
    private String currentState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            receiver_user_id = Objects.requireNonNull(getIntent().getExtras()).get(CLICKED_USER_ID).toString();
        }
        sender_user_id = mAuth.getCurrentUser().getUid();

        if (sender_user_id.equals(receiver_user_id)) {
            sendRequestButton.setVisibility(View.GONE);
            declineRequestButton.setVisibility(View.GONE);
        }

        mNotificationReference = mFirebaseDatabase.getReference().child("notification");
        mNotificationReference.keepSynced(true);

        mFriendsReference = mFirebaseDatabase.getReference().child("friends");
        mFriendsReference.keepSynced(true);

        mUserProfileReference = mFirebaseDatabase.getReference().child("users").child(receiver_user_id);
        mUserProfileReference.keepSynced(true);

        mUserProfileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                profileName.setText(user.getUser_name());
                profileStatus.setText(user.getUser_status());

                Picasso.get().load(user.getUser_image()).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.profile).into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(user.getUser_image())
                                .placeholder(R.drawable.profile).into(profileImage);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestButton.setEnabled(false);
                if (currentState.equals("not_friend")) {
                    sendFriendRequest();
                } else if (currentState.equals("coming_request")) {
                    acceptFriendRequest();
                } else if (currentState.equals("request_sent")) {
                    cancelFriendRequest();
                } else if (currentState.equals("friend")) {
                    unFriend();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentState = "not_friend";
        sendRequestButton.setText("Send Friend Request");
        mFriendRequestReference = mFirebaseDatabase.getReference().child("friend_request");
        mFriendRequestReference.keepSynced(true);
        mFriendRequestReference.child(sender_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(receiver_user_id)) {
                    String requestType = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();
                    if (requestType.equals("sent")) {
                        currentState = "request_sent";
                        sendRequestButton.setText("Cancel The Request");
                    } else if (requestType.equals("receiver")) {
                        currentState = "coming_request";
                        sendRequestButton.setText("Accept The Request");
                        declineRequestButton.setEnabled(true);
                        declineRequestButton.setVisibility(View.VISIBLE);
                        declineRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                declineRequest();
                            }
                        });
                    }
                } else {
                    mFriendsReference.child(sender_user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild(receiver_user_id)) {
                                    currentState = "friend";
                                    sendRequestButton.setText("UnFriend");
                                    sendRequestButton.setEnabled(true);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void unFriend() {
        mFriendsReference.child(receiver_user_id).child(sender_user_id)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFriendsReference.child(sender_user_id).child(receiver_user_id)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                currentState = "not_friend";
                                sendRequestButton.setText("Sent Friend Request");
                                sendRequestButton.setEnabled(true);

                            }
                        }
                    });
                }
            }
        });

    }

    private void cancelFriendRequest() {
        mFriendRequestReference.child(receiver_user_id).child(sender_user_id)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFriendRequestReference.child(sender_user_id).child(receiver_user_id)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                currentState = "not_friend";
                                sendRequestButton.setText("Sent Friend Request");
                                sendRequestButton.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });

    }

    private void declineRequest() {
        mFriendRequestReference.child(receiver_user_id).child(sender_user_id)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFriendRequestReference.child(sender_user_id).child(receiver_user_id)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                declineRequestButton.setEnabled(false);
                                declineRequestButton.setVisibility(View.GONE);
                                currentState = "not_friend";
                                sendRequestButton.setText("Sent Friend Request");
                            }
                        }
                    });
                }
            }
        });
    }

    private void isFriend() {
        mFriendRequestReference.child(receiver_user_id).child(sender_user_id)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFriendRequestReference.child(sender_user_id).child(receiver_user_id)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                currentState = "friend";
                                sendRequestButton.setText("UnFriend");
                                sendRequestButton.setEnabled(true);
                                declineRequestButton.setEnabled(false);
                                declineRequestButton.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptFriendRequest() {
        Long tsLong = System.currentTimeMillis();
        final String saveCurrentDate = tsLong.toString();
        long time = Long.parseLong(saveCurrentDate);

        String getTime = getDate(time);
        Toast.makeText(ProfileActivity.this, getTime, Toast.LENGTH_SHORT).show();

        mFriendsReference.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendsReference.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        isFriend();
                                    }
                                });
                    }
                });
    }

    private void sendFriendRequest() {
        mFriendRequestReference.child(receiver_user_id).child(sender_user_id).child("request_type")
                .setValue("receiver").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFriendRequestReference.child(sender_user_id).child(receiver_user_id).child("request_type")
                        .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, String> notificationData = new HashMap<>();
                            notificationData.put("from", sender_user_id);
                            notificationData.put("type", "friendRequest");
                            mNotificationReference.child(receiver_user_id).push().setValue(notificationData);
                            currentState = "request_sent";
                            sendRequestButton.setText("Cancel The Request");
                            sendRequestButton.setEnabled(true);

                        }
                    }
                });
            }
        });
    }

    private String getDate(long time_stamp_server) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        return formatter.format(time_stamp_server);
    }
}
