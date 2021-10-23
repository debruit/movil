package com.example.taller_3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AllOnline extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    LinearLayout listUsers,listLinear;
    ImageView imagen;
    TextView nombre;
    Button ver;

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

        changes();
    }

    public void changes(){
        myRef = database.getReference(PATH_USERS);
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listLinear.removeAllViews();
                listUsers.removeAllViews();
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    Usuario usuario = child.getValue(Usuario.class);
                    System.out.println(usuario.getDisponible());
                    if(usuario.getDisponible()){
                        nombre.setText(usuario.getNombre());
                        listUsers.addView(imagen);
                        listUsers.addView(nombre);
                        listUsers.addView(ver);
                        listLinear.addView(listUsers);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }
}