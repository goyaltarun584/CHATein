package com.example.tarungoyal.chatein;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDisplayID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String user_id = getIntent().getStringExtra("user_id");

        mDisplayID = (TextView)findViewById(R.id.profile_DisplayName);
        mDisplayID.setText(user_id);

    }
}
