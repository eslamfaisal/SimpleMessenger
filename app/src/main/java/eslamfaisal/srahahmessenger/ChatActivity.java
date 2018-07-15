package eslamfaisal.srahahmessenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eslamfaisal.srahahmessenger.modules.GetTimeAgo;
import eslamfaisal.srahahmessenger.modules.Message;
import eslamfaisal.srahahmessenger.modules.User;

public class ChatActivity extends AppCompatActivity {

    private String mMessageReceiverId;
    private String mMessageReceiverName;

    Toolbar mChatToolBar;
    TextView mUserName;
    TextView mUserLastSeen;
    CircleImageView mUserImage;

    ImageButton mSendImageButton;

    ImageButton mSendMessageButton;
    EditText mInputMessage;

    FirebaseAuth mAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mUserReference;
    DatabaseReference messagesRef;
    DatabaseReference messageReceiverRef;

    private String mMessageSenderId;

    private RecyclerView mChatRecyclerView;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mMessageReceiverId = getIntent().getExtras().get(ProfileActivity.CLICKED_USER_ID).toString();
        mMessageReceiverName = getIntent().getExtras().get("user_name").toString();

        firebaseReferences();

        initializeToolBar();

        initializeViews();

        getToolBarData();

        initializSendMessage();

        messageAdapter = new MessageAdapter(messageList);
        mChatRecyclerView = findViewById(R.id.chat_recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.setLayoutManager(linearLayoutManager);
        mChatRecyclerView.setAdapter(messageAdapter);

        // Scroll to bottom on new messages
        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                linearLayoutManager.smoothScrollToPosition(mChatRecyclerView, null, messageAdapter.getItemCount());
            }
        });
        fetchMessages();

    }

    private void fetchMessages() {
        messagesRef.child(mMessageSenderId).child(mMessageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                messageAdapter.notifyItemInserted(messageList.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void firebaseReferences() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        messagesRef = mFirebaseDatabase.getReference().child("messages");

        mUserReference = mFirebaseDatabase.getReference().child("users").child(mMessageReceiverId);
        mAuth = FirebaseAuth.getInstance();
        mMessageSenderId = mAuth.getCurrentUser().getUid();
    }

    private void initializSendMessage() {
        // Enable Send button when there's text to send
        mInputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendMessageButton.setEnabled(true);
                } else {
                    mSendMessageButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void getToolBarData() {
        mUserName.setText(mMessageReceiverName);

        mUserReference.child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String state = dataSnapshot.getValue().toString();
                if (state.equals("true")) {
                    mUserLastSeen.setText("online");
                } else {
                    long last = (long) dataSnapshot.getValue();
                    String timeAgo = GetTimeAgo.getTimeAgo(last, getApplicationContext());
                    mUserLastSeen.setText(timeAgo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    Picasso.get().load(user.getUser_image()).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profile).into(mUserImage,
                            new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(user.getUser_image())
                                            .placeholder(R.drawable.profile).into(mUserImage);
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        //  String messagesRef = "messages/"+mMessageSenderId+"/"+mMessageReceiverId;
        //  String messageReceiverRef = "messages/"+mMessageReceiverId+"/"+ mMessageSenderId;


        Long tsLong = System.currentTimeMillis();
        final String saveCurrentDate = tsLong.toString();
        long time = Long.parseLong(saveCurrentDate);

        String messageText = mInputMessage.getText().toString().trim();
        Message messageFromMe = new Message(messageText, "text", time, false, null,"fromMe");
        Message messageToMe = new Message(messageText, "text", time, false, null,"toMe");

        messagesRef.child(mMessageSenderId).child(mMessageReceiverId).push().setValue(messageFromMe);
        messagesRef.child(mMessageReceiverId).child(mMessageSenderId).push().setValue(messageToMe);

        mInputMessage.setText("");

    }

    private void initializeViews() {
        mSendImageButton = findViewById(R.id.chat_send_image);
        mSendMessageButton = findViewById(R.id.chat_send_message);
        mInputMessage = findViewById(R.id.chat_input_message);

        mUserName = findViewById(R.id.chat_profile_name);
        mUserLastSeen = findViewById(R.id.chat_profile_last_seen);
        mUserImage = findViewById(R.id.chat_profile_image);


    }

    @SuppressLint("InflateParams")
    private void initializeToolBar() {
        mChatToolBar = findViewById(R.id.chat_activity_toolbar);
        setSupportActionBar(mChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
        }
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = null;
        if (inflater != null) {
            view = inflater.inflate(R.layout.chat_custombar, null);
        }
        if (actionBar != null) {
            actionBar.setCustomView(view);
        }
    }

}
