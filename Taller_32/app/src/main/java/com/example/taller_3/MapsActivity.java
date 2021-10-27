package com.example.taller_3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.example.taller_3.modelo.Usuario;
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
import com.example.taller_3.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Bundle bundle;

    FirebaseDatabase database;
    DatabaseReference myRef;

    private String uidOU;


    public static final double RADIUS_OF_EARTH_KM = 6371;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    //Atributos de localizacion
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    //Atributo de el marcador de la localización de el usuario
    private Marker currentLocation;

    //Ubicación del usuario
    private LatLng actual;

    //marccador de la localización del otro usuario
    private Marker  currentLocationExU;

    //ubicacionn del otro usuario
    private LatLng actual2;

    ValueEventListener val;

    int locationid = 3;

    static final int REQUEST_CHECK_SETTINGS = 7;
    boolean isGPSEnabled = false;

    String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    public static final String PATH_USERS="users/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database= FirebaseDatabase.getInstance();

        bundle = getIntent().getBundleExtra("bundle");

        uidOU = bundle.getString("udi");

        requestPermission(this,locationPermission, "No lo podemos localizar sin su permiso",
                locationid);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = createLocationRequest();
        locationCallback = createLocationCallback();


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    protected void onResume() {
        super.onResume();
        checkSettingsLocation();
        changes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
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
                    // aqui dento rescribo el firebase
                    actual = new LatLng(location.getLatitude(), location.getLongitude());
                    currentLocation = mMap.addMarker(new MarkerOptions().position(actual)
                            .title("Ubicación de usuario").icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    myRef = database.getReference(PATH_USERS+currentUser.getUid()+"/latitud");
                    myRef.setValue(actual.latitude);
                    myRef = database.getReference(PATH_USERS+currentUser.getUid()+"/longitud");
                    myRef.setValue(actual.longitude);
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

    public void changes(){
        myRef = database.getReference(PATH_USERS+uidOU);
        // Read from the database
        val = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                if((!usuario.getLatitud().equals(actual2.latitude))&&(!usuario.getLongitud().equals(actual2.longitude))){
                    actual2 = new LatLng(usuario.getLatitud(), usuario.getLongitud());
                    if(currentLocationExU != null) currentLocationExU.remove();
                    currentLocationExU = mMap.addMarker(new MarkerOptions().position(actual2)
                            .title("Ubicación de usuario").icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }

        });

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
                            resolvable.startResolutionForResult(MapsActivity.this,REQUEST_CHECK_SETTINGS);
                        }catch (IntentSender.SendIntentException sendEx){

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12));
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

    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 -lat2);
        double lngDistance = Math.toRadians(long1 -long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)+ Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2))* Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 -a));
        double result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result*100.0)/100.0;
    }

}