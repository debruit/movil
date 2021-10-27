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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.taller_3.modelo.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    private static final int IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);
    public static final String PATH_USERS="users/";

    private EditText nombre,apellido,email,password,identificacion;
    private FirebaseAuth mAuth;

    Uri imageUri;

    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseStorage storage;
    StorageReference mStorageRef;

    ImageView image;
    String cameraPermission = Manifest.permission.CAMERA;
    String galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    int idCamera = 3;
    int idGallery = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        image = (ImageView)findViewById(R.id.imagen);

        nombre = findViewById(R.id.nombre);
        apellido = findViewById(R.id.apellido);
        email = findViewById(R.id.emailRegistrar);
        password = findViewById(R.id.passwordRegistrar);
        identificacion = findViewById(R.id.identificacion);

        mAuth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();

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
                        imageUri = data.getData();
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

    public void registro(View view) {
        String correo = email.getText().toString();
        String contra = password.getText().toString();
        if (validar(correo, contra)) {
            mAuth.createUserWithEmailAndPassword(correo, contra).addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(Register.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                    } else {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            Usuario usuario = new Usuario();
                            usuario.setNombre(nombre.getText().toString());
                            usuario.setApellido(apellido.getText().toString());
                            usuario.setId(Long.parseLong(identificacion.getText().toString()));
                            usuario.setLatitud(null);
                            usuario.setLongitud(null);
                            usuario.setDisponible(false);

                            uploadImg(user);

                            myRef = database.getReference(PATH_USERS+user.getUid());
                            myRef.setValue(usuario);

                            UserProfileChangeRequest.Builder upcrb = new UserProfileChangeRequest.Builder();
                            upcrb.setDisplayName(correo);
                            user.updateProfile(upcrb.build());
                            updateUI(user);
                        }
                    }
                }
            });
        }else{
            nombre.setText("");
            apellido.setText("");
            email.setText("");
            password.setText("");
            identificacion.setText("");
        }
    }

    private void updateUI(FirebaseUser currentUser){
        mAuth.signOut();
        nombre.setText("");
        apellido.setText("");
        email.setText("");
        password.setText("");
        identificacion.setText("");
        Intent intent = new Intent(getBaseContext(),MainActivity.class);
        startActivity(intent);
    }

    public boolean validateEmailId(String emailId) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailId);
        return matcher.find();
    }

    public boolean validar(String correo, String contra) {

        if(TextUtils.isEmpty(nombre.getText().toString())){
            nombre.setError("Required");
            return false;
        }
        if(TextUtils.isEmpty(apellido.getText().toString())){
            apellido.setError("Required");
            return false;
        }

        if(TextUtils.isEmpty(identificacion.getText().toString())){
            identificacion.setError("Required");
            return false;
        }

        if(TextUtils.isEmpty(correo)){
            email.setError("Required");
            return false;
        }
        if(TextUtils.isEmpty(contra)){
            password.setError("Required");
            return false;
        }

        // Email invalido
        if (!validateEmailId(correo)) {
            email.setError("Email no válido");
            return false;
        }

        // Password no puede tener espacios
        else if (!Pattern.matches("[^ ]*", contra)) {
            password.setError("La contraseña no puede contener espacios");
            return false;
        }else if(contra.length() < 5){
            password.setError("La contraseña debe ser mayor a 5 caracteres");
            return false;
        }

        return true;
    }

    private void uploadImg(FirebaseUser currentUser) {

        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

//        Uri file = uri;
        StorageReference imageRef = mStorageRef.child("images/profile/"+currentUser.getUid()+"/image.jpg");

        imageRef.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get a URL to the uploaded content
                Log.i("FBApp", "Succesfullyupload image");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads// ...}});}https://firebase.google.com/docs/storage/android/start
            }
        });
    }

}