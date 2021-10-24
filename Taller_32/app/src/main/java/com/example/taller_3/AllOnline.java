package com.example.taller_3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class AllOnline extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseStorage storage;
    StorageReference mStorageRef;

    LinearLayout listUsers,listLinear;
    ImageView imagen;
    TextView nombre;
    Button ver;
    ValueEventListener val;

    public static final String PATH_USERS="users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_online);

        database = FirebaseDatabase.getInstance();
        listUsers = findViewById(R.id.listUsers);
        imagen = findViewById(R.id.img);
        nombre = findViewById(R.id.name);
        ver = findViewById(R.id.posActual);
        listLinear = findViewById(R.id.linear);

        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        changes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(val!=null)
            myRef.removeEventListener(val);
    }

    public void changes(){
        myRef = database.getReference(PATH_USERS);
        // Read from the database
        val = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                listLinear.removeAllViews();
                listUsers.removeAllViews();
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    ImageView iv = new ImageView(AllOnline.this);
                    Usuario usuario = child.getValue(Usuario.class);
                    if(usuario.getDisponible()){
                        nombre.setText(usuario.getNombre()+" "+usuario.getApellido());
                        try {
                            iv.setImageURI(downloadFile(child.getKey()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        listUsers.addView(imagen);
                        listUsers.addView(nombre);
                        listUsers.addView(ver);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private Uri downloadFile(String key) throws IOException {
        final Uri[] uri = new Uri[1];
        File localFile= File.createTempFile("images", "jpg");
        StorageReference imageRef= mStorageRef.child("images/profile/"+key+"/image.jpg");
        imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Successfully downloaded data to local file// ...
                imagen.setImageURI(Uri.fromFile(localFile));
                uri[0] = Uri.fromFile(localFile);
                //UpdateUIusing the localFile
                }})
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle failed download// ...
                    }});
        return uri[0];
    }

}