package com.example.taller_3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class Register extends AppCompatActivity {

    ImageView image;
    String cameraPermission = Manifest.permission.CAMERA;
    String galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    int idCamera = 3;
    int idGallery = 4;

    private static final int IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        image = (ImageView)findViewById(R.id.imagen);
    }

    private void camera(){
        if (ActivityCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED){
            Intent tomarFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                startActivityForResult(tomarFoto, CAMERA_REQUEST);
            } catch (ActivityNotFoundException e){
                Log.e("PERMISSION APP",e.getMessage());
            }
        }
    }

    private void gallery(){
        if (ActivityCompat.checkSelfPermission(this, galleryPermission) == PackageManager.PERMISSION_GRANTED){
            Intent elegirImagen = new Intent(Intent.ACTION_PICK);
            elegirImagen.setType("image/*");
            startActivityForResult(elegirImagen,IMAGE_REQUEST);
        }
    }

    public void cameraPressed(View v){
        if (ActivityCompat.checkSelfPermission(this,cameraPermission)!=PackageManager.PERMISSION_GRANTED){
            requestPermission(this,cameraPermission,"",idCamera);
        }else{
            camera();
        }
    }

    public void galleryPressed(View v){
        if (ActivityCompat.checkSelfPermission(this,galleryPermission)!=PackageManager.PERMISSION_GRANTED){
            requestPermission(this,galleryPermission,"",idGallery);
        }else{
            gallery();
        }
    }

    private void requestPermission(Activity context, String permission, String justification, int id){
        if(ContextCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(context,permission)){
                Toast.makeText(context,justification,Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(context,new String[]{permission},id);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == idCamera){
            camera();
        } else if(requestCode == idGallery){
            gallery();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case IMAGE_REQUEST:
                if (resultCode==RESULT_OK){
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        image.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                };
                break;
            case CAMERA_REQUEST:
                if (resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    image.setImageBitmap(imageBitmap);
                }
        }
    }
}