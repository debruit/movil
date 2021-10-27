package com.example.taller_3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.taller_3.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class authDone extends FragmentActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public static final String PATH_USERS="users/";

    FirebaseDatabase database;
    DatabaseReference myRef;

    boolean cambio=false;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    //Atributos de localizacion
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    //Atributo de el marcador de la localización de el usuario
    private Marker currentLocation;

    //Atributo de puntos de localización
    private Marker fiveP;

    //Ubicación del usuario
    private LatLng actual;

    int locationid = 3;

    static final int REQUEST_CHECK_SETTINGS = 7;
    boolean isGPSEnabled = false;

    String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    JSONArray locationsJsonArray;
    JSONObject jsonObject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_auth_done);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database= FirebaseDatabase.getInstance();

        requestPermission(this,locationPermission, "No lo podemos localizar sin su permiso",
                locationid);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = createLocationRequest();
        locationCallback = createLocationCallback();


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_auth_done);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapauthDone);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSettingsLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12));

        addFiveMarkers();
    }

    public void addFiveMarkers() {
        JSONObject json = null;

        try {
            json = new JSONObject(loadJSONFromAsset());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            locationsJsonArray = json.getJSONArray("locationsArray");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        for (int i = 0; i < locationsJsonArray.length(); i++) {
            try {
                jsonObject = locationsJsonArray.getJSONObject(i);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            try {
                LatLng location = new LatLng(jsonObject.getDouble("latitude"),jsonObject.getDouble("longitude"));
                fiveP = mMap.addMarker(new MarkerOptions().position(location)
                                .title(jsonObject.getString("name")).icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }catch (JSONException ex){
                ex.printStackTrace();
            }

        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private LocationRequest createLocationRequest(){

        LocationRequest locationRequest = LocationRequest.create().setInterval(60000)
                .setFastestInterval(30000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    private LocationCallback createLocationCallback(){
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                boolean ubicar = false;
                Location location = locationResult.getLastLocation();
                if (location != null){

                    if(currentLocation != null) currentLocation.remove();

                    actual = new LatLng(location.getLatitude(), location.getLongitude());
                    currentLocation = mMap.addMarker(new MarkerOptions().position(actual)
                            .title("Ubicación de usuario").icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    if(ubicar == false){
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(actual));
                        ubicar = true;
                    }

                }

            }
        };
        return locationCallback;
    }

    private void  startLocationUpdates(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void checkSettingsLocation(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                isGPSEnabled=true;
                startLocationUpdates();
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                isGPSEnabled=false;
                switch (statusCode){
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(authDone.this,REQUEST_CHECK_SETTINGS);
                        }catch (IntentSender.SendIntentException sendEx){

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }
    private void requestPermission(Activity context, String  permission, String justification, int id){

        if(ContextCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(context,permission)){
                Toast.makeText(context,justification,Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(context,new String[]{permission},id);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == this.locationid){
            //initView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS){
            if (resultCode == RESULT_OK){
                isGPSEnabled = true;
                startLocationUpdates();
            }else {
                Toast.makeText(this, "GPS no activado", Toast.LENGTH_LONG).show();
            }
        }
    }
}