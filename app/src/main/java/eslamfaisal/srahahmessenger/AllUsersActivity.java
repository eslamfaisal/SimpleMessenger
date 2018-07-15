package eslamfaisal.srahahmessenger;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import eslamfaisal.srahahmessenger.modules.User;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private DatabaseReference allUsersReference;

    ChildEventListener childEventListener;
    Query query;
    FirebaseRecyclerOptions<User> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = findViewById(R.id.all_users_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        allUsersReference = FirebaseDatabase.getInstance().getReference().child("users");
        allUsersReference.keepSynced(true);
        query = FirebaseDatabase.getInstance()
                .getReference()
                .child("users");
//                .orderByChild("user_name")
//                .equalTo("eslam")
//                .limitToLast(5);


        options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<User, AllUsersViewHolder>(options) {
            @Override
            public AllUsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_list, parent, false);

                return new AllUsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final AllUsersViewHolder holder, final int position, @NonNull User model) {

                holder.userName.setText(model.getUser_name());
                final Uri imageUri = Uri.parse(model.getUser_thumb_image());
                Picasso.get().load(imageUri).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.profile).into(holder.userImage,
                        new Callback() {
                            @Override
                            public void onSuccess() {}
                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(imageUri).placeholder(R.drawable.profile).into(holder.userImage);
                            }
                        });
                holder.userStatus.setText(model.getUser_status());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id = getRef(position).getKey();
                        Intent intent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                    }
                });
            }


        };
        mRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }



    public class AllUsersViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userStatus;
        CircleImageView userImage;
        View mView;

        public AllUsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            userName = itemView.findViewById(R.id.name_all_users_profile);

            userImage = itemView.findViewById(R.id.image_all_users_profile);

            userStatus = itemView.findViewById(R.id.status_all_users_profile);


        }

    }

    public void refresh(View view) {

    }

}
