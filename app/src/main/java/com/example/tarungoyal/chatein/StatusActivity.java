package com.example.tarungoyal.chatein;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mSaveBtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        mToolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.status_appbar);
         setSupportActionBar(mToolbar);
         getSupportActionBar().setTitle("Account Status");
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         String Status_value = getIntent().getStringExtra("status_value");




         mStatus = (TextInputLayout)findViewById(R.id.status_input);
         mSaveBtn = (Button)findViewById(R.id.status_savebtn);

         mStatus.getEditText().setText(Status_value);

         mSaveBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 mProgress = new ProgressDialog(StatusActivity.this);
                 mProgress.setTitle("Saving Changes");
                 mProgress.setMessage("Please wait while we make changes");
                 mProgress.show();

                 String status = mStatus.getEditText().getText().toString();


                 mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {

                         if(task.isSuccessful()){
                             mProgress.dismiss();
                         }else{
                             Toast.makeText(getApplicationContext(), "There was some error in saving changes", Toast.LENGTH_LONG).show();
                         }
                     }
                 });
             }
         });


    }


}
