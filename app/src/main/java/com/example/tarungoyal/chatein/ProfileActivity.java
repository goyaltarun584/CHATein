package com.example.tarungoyal.chatein;

import android.app.ProgressDialog;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mFriendsCount;
    private Button mProfileSendRequestBtn;
    private DatabaseReference mUsersDatabase;
    private ProgressDialog mProgressDialog;
    private String mcurrent_state;
    private DatabaseReference mFriendReqDatabase;
    private FirebaseUser mCurrent_user;
    private DatabaseReference mFriendDatabase;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

         mProfileImage = (ImageView)findViewById(R.id.profile_image);
         mProfileName = (TextView)findViewById(R.id.profile_DisplayName);
        mProfileStatus = (TextView)findViewById(R.id.profile_status);
        mFriendsCount = (TextView)findViewById(R.id.profile_totalFriends);
        mProfileSendRequestBtn = (Button)findViewById(R.id.profile_send_req_btn);

        mcurrent_state = "not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load users's data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String Display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image  = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(Display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);


                // ------------------- FRIEND LIST REQUEST FEATURE ------------

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){
                                mcurrent_state = "req_received";
                                mProfileSendRequestBtn.setText("ACCEPT FRIEND REQUEST");
                            }else if(req_type.equals("sent")) {
                                mcurrent_state = "req_sent";
                                mProfileSendRequestBtn.setText("CANCEL FRIEND REQUEST");

                            }
                            mProgressDialog.dismiss();
                        }else{
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){

                                        mcurrent_state = "friends";
                                        mProfileSendRequestBtn.setText(" UNFRIEND THIS PERSON");
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendRequestBtn.setEnabled(false);

                // ----------- NOT FRIENDS STATE-----------

                if(mcurrent_state.equals("not_friends")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {


                            if(task.isSuccessful()){

                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mProfileSendRequestBtn.setEnabled(true);
                                        mcurrent_state = "req_sent";
                                        mProfileSendRequestBtn.setText("CANCEL FRIEND REQUEST");
                                        //Toast.makeText(ProfileActivity.this, "Request sent successfully", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                if(mcurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendRequestBtn.setEnabled(true);
                                    mcurrent_state = "not_friends";
                                    mProfileSendRequestBtn.setText("SEND FRIEND REQUEST");
                                }
                            });
                        }
                    });

                }

                // -------------------REQ RECEIVED STATE----------------

                if(mcurrent_state.equals("req_received")){
                    final String CurrentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id)
                            .setValue(CurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid())
                                    .setValue(CurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mProfileSendRequestBtn.setEnabled(true);
                                                                    mcurrent_state = "friends";
                                                                    mProfileSendRequestBtn.setText(" UNFRIEND THIS PERSON");
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}
