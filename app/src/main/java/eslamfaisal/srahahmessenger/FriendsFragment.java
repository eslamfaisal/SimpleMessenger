package eslamfaisal.srahahmessenger;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import de.hdodenhof.circleimageview.CircleImageView;
import eslamfaisal.srahahmessenger.modules.Friend;
import eslamfaisal.srahahmessenger.modules.User;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private String onlineUserID;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersReference;

    private View mFriendsView;
    private RecyclerView mFriendsRecyclerView;

    private Query query;
    private FirebaseRecyclerOptions<Friend> options;


    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        onlineUserID = mAuth.getCurrentUser().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersReference.keepSynced(true);
        mUsersReference.keepSynced(true);

        query = mFirebaseDatabase
                .getReference()
                .child("friends")
                .child(onlineUserID);
        query.keepSynced(true);
//                .orderByChild("user_name")
//                .equalTo("eslam")

        options =
                new FirebaseRecyclerOptions.Builder<Friend>()
                        .setQuery(query, Friend.class)
                        .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mFriendsView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsRecyclerView = mFriendsView.findViewById(R.id.friends_recyclerview);
        mFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        return mFriendsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Friend, FriendsViewHolder>(options) {


            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_list, parent, false);

                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friend model) {

                final String userId = getRef(position).getKey();
                mUsersReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);

                        holder.userName.setText(user.getUser_name());
                        final Uri imageUri = Uri.parse(user.getUser_thumb_image());
                        Picasso.get().load(imageUri).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(holder.userImage,
                                new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(imageUri).placeholder(R.drawable.profile).into(holder.userImage);
                                    }
                                });

                        long longDate = Long.parseLong(model.getDate());
                        String date = getDate(longDate);
                        holder.userStatus.setText(date);
                        mUsersReference.child(userId).child("online").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String state = dataSnapshot.getValue().toString();
                                if (state.equals("true")) {
                                    holder.accountState.setImageResource(R.drawable.ic_chape_online);
                                } else {
                                    holder.accountState.setImageResource(R.drawable.ic_chape_offline);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence[] options = new CharSequence[]{user.getUser_name() + "'s Profile", "Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int position) {
                                        if (position == 0) {
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra(ProfileActivity.CLICKED_USER_ID, userId);
                                            startActivity(profileIntent);
                                        } else {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra(ProfileActivity.CLICKED_USER_ID, userId);
                                            chatIntent.putExtra("user_name", user.getUser_name());
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.create();
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        };

        mFriendsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userStatus;
        CircleImageView userImage;
        View mView;
        ImageView accountState;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            accountState = itemView.findViewById(R.id.account_state);
            userName = itemView.findViewById(R.id.name_all_users_profile);

            userImage = itemView.findViewById(R.id.image_all_users_profile);

            userStatus = itemView.findViewById(R.id.status_all_users_profile);

        }

    }


    private String getDate(long time_stamp_server) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd- MM- yyyy");
        return formatter.format(time_stamp_server);
    }
}
