package com.example.taller_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ListView;
import android.widget.Toast;

import com.example.taller_2.adapters.contactsAdapter;

public class contactoActivity extends AppCompatActivity {

    ListView listContacts;
    contactsAdapter adapter;

    String contactsPermission = Manifest.permission.READ_CONTACTS;
    public static final int CONTACTS_ID = 0;

    String[] projection = new String[]{ContactsContract.Profile._ID, ContactsContract.Profile.DISPLAY_NAME_PRIMARY};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacto);

        listContacts=(ListView)findViewById(R.id.listContacts);
        adapter = new contactsAdapter(this,null,0);

        listContacts.setAdapter(adapter);

        requestPermission(this, contactsPermission, "Cualquier cosa", CONTACTS_ID);

        initView();
    }

    private void initView(){
        if (ContextCompat.checkSelfPermission(this, contactsPermission) == PackageManager.PERMISSION_GRANTED){
            Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null, null,null);
            adapter.changeCursor(cursor);
        }else{

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
        if(requestCode == CONTACTS_ID){
            initView();
        }
    }
}