package com.example.filmish;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class VideoActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private EditText titleEt;
    private VideoView videoView;
    private Button uploadVideoBtn;
    private FloatingActionButton pickVideo;

    private static final int VIDEO_PICK_GALLERY_CODE=100;
    private static final int VIDEO_PICK_CAMERA_CODE=101;
    private static final int CAMERA_REQUEST_CODE=102;

    private String[] cameraPermissions;
    private Uri videoUrl = null;
    private String title;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add new video");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        titleEt = findViewById(R.id.titleEt);
        videoView = findViewById(R.id.videoView);
        uploadVideoBtn = findViewById(R.id.uploadVideoBtn);
        pickVideo = findViewById(R.id.pickVideo);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Uploading video");
        progressDialog.setCanceledOnTouchOutside(false);



        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        uploadVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 title= titleEt.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(VideoActivity.this,"Title is required",Toast.LENGTH_SHORT).show();
                }
                else if(videoUrl == null){
                    Toast.makeText(VideoActivity.this,"Pick a video before you can upload",Toast.LENGTH_SHORT).show();


                }
                else{
                    uploadVideoFirebase();

                }

            }
        });

        pickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoPickDialog();
            }
        });
    }

    private void uploadVideoFirebase() {
        progressDialog.show();

        String timestamp = ""+ System.currentTimeMillis();
        String filePathAndName = "Videos/"+"video_"+ timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(videoUrl)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        if(uriTask.isSuccessful()){
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("id",""+ timestamp);
                            hashMap.put("title",title);
                            hashMap.put("timestamp","" + timestamp);
                            hashMap.put("videoUrl",""+ downloadUri);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Videos");
                            reference.child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(VideoActivity.this, "Video Uploaded", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(VideoActivity.this, ""+e.getMessage()
                                                    , Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }

                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(VideoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void videoPickDialog() {
        String[] options = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Video From")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            if(checkCameraPermission()){
                                requestCameraPermission();
                            }else{
                                videoPickCamera();
                            }

                        }
                        else if(i==1){
                            videoPickGallery();


                        }

                    }
                })
                .show();

    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private Boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this,Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED;
        return result1 && result2;
    }

    private void videoPickGallery(){
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Videos"),VIDEO_PICK_GALLERY_CODE);
    }

    private void videoPickCamera(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent,VIDEO_PICK_CAMERA_CODE);
    }

    private void setVideoToVideoView(){
        MediaController mediaController=new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(videoUrl);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.pause();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean cameraAccepted =grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted =grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        videoPickCamera();
                    }else{
                        Toast.makeText(this,"Camera and storage permission are required",Toast.LENGTH_SHORT).show();
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == VIDEO_PICK_GALLERY_CODE){
                videoUrl = data.getData();
                setVideoToVideoView();
            }
            else if(requestCode == VIDEO_PICK_CAMERA_CODE){
                videoUrl= data.getData();
                setVideoToVideoView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}