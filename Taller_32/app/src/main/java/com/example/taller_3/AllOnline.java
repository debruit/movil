package com.example.taller_3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.taller_3.modelo.Usuario;
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
import java.util.ArrayList;

public class AllOnline extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseStorage storage;
    StorageReference mStorageRef;
    ArrayList<String> names = new ArrayList<>();
    ArrayList<Uri> sUris = new ArrayList<>();

    AdapterS adp;

    ListView list;
    ValueEventListener val;

    public static final String PATH_USERS="users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_online);

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
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.users,parent,false);

            ImageView imgView = row.findViewById(R.id.img);
            TextView name = row.findViewById(R.id.name);
            Button button = row.findViewById(R.id.ver);

            imgView.setImageURI(this.foto.get(position));
            name.setText(this.nombreA.get(position));

            return row;
        }
    }

}