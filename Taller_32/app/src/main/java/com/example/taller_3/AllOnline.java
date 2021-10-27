package com.example.taller_3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taller_3.modelo.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.ArrayList;

public class AllOnline extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseStorage storage;
    StorageReference mStorageRef;
    ArrayList<String> names = new ArrayList<>();
    ArrayList<Uri> sUris = new ArrayList<>();

    Button button;

    AdapterS adp;

    ListView list;
    ValueEventListener val;

    boolean cambio=false;
    String keyUser;

    public static final String PATH_USERS="users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_online);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        database = FirebaseDatabase.getInstance();

        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();

        list = findViewById(R.id.list);

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
            Intent intent = new Intent(AllOnline.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (itemClicked == R.id.online) {
            myRef = database.getReference(PATH_USERS+currentUser.getUid()+"/disponible");
            if(cambio){
                cambio=false;
                myRef.setValue(false);
                Toast.makeText(AllOnline.this,"Estado cambiado a No Disponible",Toast.LENGTH_LONG).show();
            }else {
                cambio=true;
                myRef.setValue(true);
                Toast.makeText(AllOnline.this,"Estado cambiado a Disponible",Toast.LENGTH_LONG).show();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public void changes(){
        myRef = database.getReference(PATH_USERS);
        // Read from the database
        val = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                names.clear();
                sUris.clear();
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    Usuario usuario = child.getValue(Usuario.class);
                    if(usuario.getDisponible()){
                        keyUser = child.getKey();
                        String nm = usuario.getNombre()+" "+usuario.getApellido();
                        try {
                            downloadFile(child.getKey(),nm);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                list.setAdapter(adp);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }

        });

    }

    private void downloadFile(String key, String nam) throws IOException {
        File localFile= File.createTempFile("images", "jpg");
        StorageReference imageRef= mStorageRef.child("images/profile/"+key+"/image.jpg");
        imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                // Successfully downloaded data to local file//
                sUris.add(Uri.fromFile(localFile));
                names.add(nam);
                adp = new AdapterS(AllOnline.this,names,sUris);
                list.setAdapter(adp);

                }})
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle failed download// ...
                    }});
    }

    class AdapterS extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> nombreA;
        ArrayList<Uri> foto;

        AdapterS(Context c, ArrayList<String> name, ArrayList<Uri> uri){
            super(c, R.layout.users, R.id.name,name);
            this.context = c;
            this.nombreA = name;
            this.foto = uri;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View row = layoutInflater.inflate(R.layout.users,parent,false);

            ImageView imgView = row.findViewById(R.id.img);
            TextView name = row.findViewById(R.id.name);
            Button buttonS = row.findViewById(R.id.ver);
            buttonS.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("udi",keyUser);
                    Intent intent = new Intent(v.getContext(),MapsActivity.class);
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                }
            });

            imgView.setImageURI(this.foto.get(position));
            name.setText(this.nombreA.get(position));

            return row;
        }
    }

}