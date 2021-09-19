package com.example.taller_2;

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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.taller_2.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final double RADIUS_OF_EARTH_KM = 6371;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    //Atributos de localizacion
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    //Atributo de GeoCoder
    Geocoder mGeocoder;

    //Atributo de el marcador de la localización de el usuario
    private Marker currentLocation;

    //Atributos de amarcadores para GeoCoder
    private Marker marcadorBusqueda;
    private Marker marcadorTactil;

    //Ubicación del usuario
    private LatLng actual;

    int locationid = 3;

    static final int REQUEST_CHECK_SETTINGS = 7;
    boolean isGPSEnabled = false;

    String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    private EditText busqueda;

    //Sensor de luz
    SensorManager sensorManager;
    Sensor lightSensor;
    SensorEventListener lightSensorListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermission(this,locationPermission, "No lo podemos localizar sin su permiso",
                locationid);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lightSensorListener = createLightListener();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = createLocationRequest();
        locationCallback = createLocationCallback();

        mGeocoder = new Geocoder(this);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        busqueda =findViewById(R.id.Direccion);
        createSearchListener();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSettingsLocation();
        sensorManager.registerListener(this.lightSensorListener,lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        sensorManager.unregisterListener(this.lightSensorListener);
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
                                    .fromResource(R.drawable.racer)));
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
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                String nombre = searchByLocation(latLng.latitude, latLng.longitude);
                if(!"".equals(nombre)){
                    if(marcadorTactil != null) marcadorTactil.remove();
                    marcadorTactil = mMap.addMarker(new MarkerOptions().position(latLng).title(nombre)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    Toast.makeText(MapsActivity.this, "Distancia: "+ distance(
                            actual.latitude, actual.longitude, latLng.latitude,
                            latLng.longitude )+" Km", Toast.LENGTH_LONG).show();
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

    private SensorEventListener createLightListener(){
        SensorEventListener sen = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (mMap!= null) {
                    if (event.values[0] < 300) {
                        Log.i("MAPS", "DARK MAP " + event.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this,
                                R.raw.darkmap));
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this,
                                R.raw.lightmap));
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
        return sen;
    }

    private void createSearchListener(){
        busqueda.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    String direccion = busqueda.getText().toString();
                    LatLng ubicación = searchByname(direccion);
                    if(ubicación != null && mMap != null){
                        if(marcadorBusqueda != null) marcadorBusqueda.remove();
                        marcadorBusqueda = mMap.addMarker(new MarkerOptions().position(ubicación)
                                .title(direccion).icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicación));
                        Toast.makeText(MapsActivity.this, "Distancia: "+ distance(
                                actual.latitude, actual.longitude, ubicación.latitude,
                                ubicación.longitude )+" Km", Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            }
        });
    }

    private LatLng searchByname(String direccion){
        LatLng ubicacion = null;
        if(!direccion.isEmpty()){
            try {
                List<Address> addresses = mGeocoder.getFromLocationName(direccion,2);
                if(addresses != null && !addresses.isEmpty()){
                    Address addressResult = addresses.get(0);
                    ubicacion = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                }else {
                    Toast.makeText(MapsActivity.this, "direccion no encontrada", Toast.LENGTH_LONG);
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return ubicacion;
    }
    private String searchByLocation(double lat, double lon){
        String nombre=null;
        try {
            List<Address> addresses = mGeocoder.getFromLocation(lat,lon, 2);
            if(addresses != null && !addresses.isEmpty()){
                Address addressResult = addresses.get(0);
                nombre = addressResult.getFeatureName();
            }else {
                Toast.makeText(MapsActivity.this, "direccion no encontrada", Toast.LENGTH_LONG);
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
        return nombre;
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