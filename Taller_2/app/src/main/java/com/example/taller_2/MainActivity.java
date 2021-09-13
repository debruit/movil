package com.example.taller_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void ir_contactos(View v){
        startActivity(new Intent(this,contactoActivity.class));
    }

    public void ir_camara(View v){
        startActivity(new Intent(this,imagenesActivity.class));
    }
}