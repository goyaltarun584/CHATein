package com.example.tarungoyal.chatein;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    //Firebase Database Instance.
    private DatabaseReference mUserDataBase;

    //Firebase User Instance.
    private FirebaseUser mCurrentUser;

    private StorageReference mImageStorage;

    //Widgets Instance variables.
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private CircleImageView mThumb_Image;
    private Button mChangeStatus;
    private Button mChangeImgBtn;

    private static final int GALLERY_PICK = 1;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Finding Widgets with their Unique Id's
        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mChangeStatus = (Button) findViewById(R.id.settings_status_button);
        mChangeImgBtn = (Button) findViewById(R.id.settings_image_button);

        //Getting The Instance of Current User.
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Getting The UID of the Current User.
        String current_uid = mCurrentUser.getUid();

        //Getting the Storage Reference from the FirebaseStorage.
        mImageStorage = FirebaseStorage.getInstance().getReference();

        //Populating the RealTimeDatabase with USERS --> UID.
        mUserDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //Setting Up An ValueEventListener.
        mUserDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Getting The Values(Child of USERS) and storing them in a String.
                String name = dataSnapshot.child("name").getValue().toString();
                String user_image = dataSnapshot.child("user_image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                //Setting up in the UI.
                mName.setText(name);
                mStatus.setText(status);

                if(!user_image.equals("default")) {
                    Picasso.get().load(user_image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Setting up an OnClickListener on Change Status Button.
        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Getting the status from the realtime database and storing it in a string.
                String status_display = mStatus.getText().toString();

                //Making an Intent to Move the user to SettingsActivity --> StatusActivity.
                Intent changestatusIntent = new Intent(SettingsActivity.this , StatusActivity.class);
                changestatusIntent.putExtra("status_value" , status_display);
                startActivity(changestatusIntent);

            }
        });

        //Setting up an OnClickListener on an ImageButton.
        mChangeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Using an Intent to get all the images from the gallery.
                Intent galleryIntent = new Intent();

                //Setting up the path of the image source.
                galleryIntent.setType("image/*");

                //Getting the content(images) from the above path.
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                //Returning the Activity with a result(selected Image).
                startActivityForResult(Intent.createChooser(galleryIntent , "SELECT IMAGE") , GALLERY_PICK);

            }
        });

    }

    //Overriding a Method to get the result.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Checking if the selected image is true or not and the result is Ok or not.
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            //Getting the Data of the Image and saving it in a Uri.
            Uri imageUri = data.getData();

            //Instantiating the cropImage feature and setting the ratio in 1:1.
            CropImage.activity(imageUri).setAspectRatio(1 , 1)
                    .setMinCropWindowSize(500,500)
                    .start(SettingsActivity.this);

        }

        //Checking if the image is cropped or not.
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //Checking if the result is Ok or not, if yes we will store the image in a uri.
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);

                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please Wait While We Upload Your Image");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                //Getting the Current UID of the User and storing it in a String.
                final String uid_img = mCurrentUser.getUid();

                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                //Saving the image in the Firebase Storage and naming the child with the UID.
                final StorageReference filepath = mImageStorage.child("profile_images").child(uid_img+".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(uid_img+".jpg");

                //If the resultUri is nor Empty or NULL.
                if (resultUri != null) {

                    //We Will setup an OnCompleteListener to store the image in the desired location in the storage.
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            //If the task is Successful we will display a toast.
                            if (task.isSuccessful()){

                                mImageStorage.child("profile_images").child("user_image.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        final String downloadUrl = uri.toString();
                                        UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> thumb_task) {

                                                mImageStorage.child("profile_images").child("thumbs").child(uid_img+".jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Uri> task) {

                                                        if(thumb_task.isSuccessful()){

                                                            Map update_Hashmap = new HashMap();
                                                            update_Hashmap.put("image",downloadUrl);

                                                    mUserDataBase.updateChildren(update_Hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()){

                                                                mProgressDialog.dismiss();

                                                            }

                                                        }
                                                    });
                                                }else{

                                                    Toast.makeText(SettingsActivity.this , "Error in uploading thumbnail" , Toast.LENGTH_LONG).show();

                                                    mProgressDialog.dismiss();
                                                }

                                                    }
                                                });

                                            }
                                        });



                                    }
                                });


                            }else {

                                Toast.makeText(SettingsActivity.this , "Error" , Toast.LENGTH_LONG).show();

                                mProgressDialog.dismiss();

                            }
                        }
                    });
                }

                //If the task is not successful then we will display an Error Message.
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
        }

    }

}




