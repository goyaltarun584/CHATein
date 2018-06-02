package com.example.tarungoyal.chatein;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdaptor mSectionsPagerAdaptor;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager)findViewById(R.id.main_tabPager);
        mSectionsPagerAdaptor = new SectionsPagerAdaptor(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdaptor);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.main_tabs);

        mTabLayout.setupWithViewPager(mViewPager);

        mToolbar =  (android.support.v7.widget.Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("CHATein");

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser==null){
            SendtoStart();
        }

    }

    private void SendtoStart(){
        Intent StartIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(StartIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);

         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId()== R.id.main_logout_btn){
             FirebaseAuth.getInstance().signOut();
             SendtoStart();
         }

         if(item.getItemId() == R.id.main_settings_btn){
             Intent settingsIntent = new Intent(MainActivity.this, in.tvac.akshaye.lapitchat.SettingsActivity.class);
             startActivity(settingsIntent);
         }

         if(item.getItemId() == R.id.main_all_btn){
             Intent userIntent = new Intent(MainActivity.this,UsersActivity.class);
             startActivity(userIntent);
         }


         return true;
    }

}
