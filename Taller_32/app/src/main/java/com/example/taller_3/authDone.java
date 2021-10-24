package com.example.taller_3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class authDone extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public static final String PATH_USERS="users/";

    FirebaseDatabase database;
    DatabaseReference myRef;

    boolean cambio=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_done);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database= FirebaseDatabase.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemClicked = item.getItemId();
        if (itemClicked == R.id.salir) {
            mAuth.signOut();
            Intent intent = new Intent(authDone.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (itemClicked == R.id.online) {
            myRef = database.getReference(PATH_USERS+currentUser.getUid()+"/disponible");
            if(cambio){
                cambio=false;
                myRef.setValue(false);
                Toast.makeText(authDone.this,"Estado cambiado a No Disponible",Toast.LENGTH_LONG).show();
            }else {
                cambio=true;
                myRef.setValue(true);
                Toast.makeText(authDone.this,"Estado cambiado a Disponible",Toast.LENGTH_LONG).show();
            }

        } else if (itemClicked == R.id.allOnline) {
            startActivity(new Intent(authDone.this, AllOnline.class));
        }
        return super.onOptionsItemSelected(item);
    }
}