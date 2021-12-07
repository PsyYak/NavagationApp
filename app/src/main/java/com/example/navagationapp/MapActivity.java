package com.example.navagationapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // check permission
        getLocationPermission();
    }

    private void getDeviceLocation() {
        // get current location
        Log.d(TAG, "getDeviceLocation: getting the device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        try {
            if (mLocationPermissionsGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: current location null");
                            Toast.makeText(MapActivity.this, "Could not get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: error: " + e.getMessage());


        }


    }

    private void moveCamera(LatLng latLng, float zoom) {

        Log.d(TAG, "moveCamera: moving camera to Latitude:" + latLng.latitude + ",Longitude:" + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));


    }

    private void initMap() {
        Log.d("initMap", "initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                Toast.makeText(MapActivity.this, "Map Ready", Toast.LENGTH_SHORT).show();
                Log.d("onMapReady", "Map starting....");
                mMap = googleMap;
                if (mLocationPermissionsGranted) {
                    getDeviceLocation();

                    if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                         && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true); // Add blue dot on map to show current location
                    mMap.getUiSettings().setMyLocationButtonEnabled(false); // disable the API auto location button

                }
            }
        });

    }

    private void getLocationPermission(){
        Log.d("getLocationPermission", "getting permission checked");
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    mLocationPermissionsGranted = true;
                    initMap();
            }else{
                ActivityCompat.requestPermissions(this,permission,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,permission,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    // loop the results
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;

                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    // init map, all permission is good
                    initMap();
                }
            }

        }
    }
}
