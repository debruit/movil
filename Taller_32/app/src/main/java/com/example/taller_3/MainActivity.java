package com.example.taller_3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

    }

    public void registrarse(View v){
        startActivity(new Intent(this,Register.class));
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent intent = new Intent(getBaseContext(), authDone.class);
            intent.putExtra("user", currentUser.getEmail());
            startActivity(intent);
        }
    }

    public void ingreso(View v) {
        String correo = email.getText().toString();
        String contra = password.getText().toString();

        if (validar(correo, contra)) {
            mAuth.signInWithEmailAndPassword(correo, contra)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Log.e("ERR",task.getException().toString());
                                Toast.makeText(MainActivity.this, "Usuario o contraseña inválidos",
                                        Toast.LENGTH_LONG).show();
                                email.setText("");
                                password.setText("");
                            } else {
                                email.setText("");
                                password.setText("");
                                Intent intent = new Intent(MainActivity.this, authDone.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    });
        } else {
            email.setText("");
            password.setText("");
        }
    }
    /*
     * Validacion de correo y contraseña
     *
     *
     */

    public boolean validateEmailId(String emailId) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailId);
        return matcher.find();
    }

    public boolean validar(String correo, String contra) {

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
}