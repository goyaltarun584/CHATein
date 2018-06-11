package com.example.tarungoyal.chatein;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;
    private DatabaseReference mUsersDatabase;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child("mCurrent_user_id");
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();
        startListening();

    }

    public void startListening() {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friends")
                .limitToLast(50);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query , Friends.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter< Friends, FriendsViewHolder>(options) {
            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final FriendsViewHolder holder, int position, final Friends model) {
                // Bind the Chat object to the ChatHolder
                holder.setDate(model.date);

                // ...

                String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        FriendsViewHolder.setName(userName);
                        FriendsViewHolder.setUserImage(userThumb);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

        };
        mFriendsList.setAdapter(adapter);
        adapter.startListening();
    }

        public static class FriendsViewHolder extends RecyclerView.ViewHolder {

            @SuppressLint("StaticFieldLeak")
            static View mView;

            public FriendsViewHolder(View itemView) {

                super(itemView);
                mView = itemView;
            }

            public void setDate(String date) {
                TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
                userStatusView.setText(date);
            }

            public static void setName(String name){
                TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
                userNameView.setText(name);
            }
            public static void setUserImage(String thumb_image){

                CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);

                Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);

            }


        }
    }

