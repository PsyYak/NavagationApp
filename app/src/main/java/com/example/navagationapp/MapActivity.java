package com.example.navagationapp;

import static com.example.navagationapp.BuildConfig.google_map_API_key;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.navagationapp.Adapter.PlaceAutoCompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168),new LatLng(71,136));

    // widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;

    // vars
    private Place mPlace;
    private PlacesClient placesClient;
    private boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutoCompleteAdapter mPlaceAutoCompleteAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText = findViewById(R.id.input_search);
        mGps = findViewById(R.id.ic_gps);

        // check permission on activity start
        getLocationPermission();
        if (!Places.isInitialized()) {
            Locale locale = new Locale("he");
            Places.initialize(getApplicationContext(), google_map_API_key, locale);
        }

    }

    private void getDeviceLocation() {
        // get current location
        Log.d(TAG, "getDeviceLocation: getting the device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        try {
            if (mLocationPermissionsGranted) {
                @SuppressLint("MissingPermission")
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");

                        } else {
                            Log.d(TAG, "onComplete: current location null");
                            Toast.makeText(MapActivity.this, "Could not get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: error: " + e.getMessage());
        }catch(NullPointerException e){
            Log.d(TAG, "getDeviceLocation: null error: "+e.getMessage());
        }


    }

    private void moveCamera(LatLng latLng, float zoom,String title) {

        Log.d(TAG, "moveCamera: moving camera to Latitude:" + latLng.latitude + ",Longitude:" + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
        hideKeyBoard();
       //hideKeyboard(MapActivity.this);
    }

    private void init(){
        Log.d(TAG, "init: initializing");

        AutocompleteSessionToken autocompleteSessionToken;
        autocompleteSessionToken=AutocompleteSessionToken.newInstance();


        placesClient=Places.createClient(this);
        mPlaceAutoCompleteAdapter = new PlaceAutoCompleteAdapter(this, placesClient,autocompleteSessionToken);

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        mSearchText.setAdapter(mPlaceAutoCompleteAdapter);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH
                   || actionId == EditorInfo.IME_ACTION_DONE
                   || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                   || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    // execute search geoLocate method
                    geoLocate();
                    hideKeyBoard();
                    //hideKeyboard(MapActivity.this);
                    mSearchText.getText().clear();
                    mSearchText.requestFocus();

                }
                return false;
            }
        });
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });
        hideKeyBoard();
        //hideKeyboard(MapActivity.this);
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: get locating");
        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        try {
                list = geocoder.getFromLocationName(searchString,1);
        }catch(IOException e){
            Log.d(TAG, "geoLocate: IOException: "+e.getMessage());
        }

        if(list.size()>0){
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: Address is: "+address.toString());
            // calling moveCamera to move the camera to that location
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
        }

    }

    private void initMap() {
        Log.d("initMap", "initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
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

                    init();

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

    // Method to hide the keyboard on activity start and after search
    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void hideKeyBoard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /*
     ---------------------------------    Google Places API autocomplete suggestion logic     --------------------------------------------
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener =  new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideKeyBoard();

            // get the item that was clicked
            final AutocompletePrediction item = mPlaceAutoCompleteAdapter.getItem(i);
            // get the place id from that item
            final String placeId = item.getPlaceId();
            Log.d(TAG, "mAutocompleteClickListener onclick: "+placeId);

            // construct the array of fields we want from that place
            List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.ADDRESS,
                    Place.Field.ID,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.LAT_LNG);
            // build the request to get the data we want based on the placeId
            FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                    .build();
            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                Log.d(TAG, "fetchPlace: Address: "+place.getAddress()+", ID:"+place.getId()+", Phone:"+place.getPhoneNumber()+", LAT_LNG: "+place.getLatLng());
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            });

        }
    };

    private void fetchPlace(FetchPlaceRequest request){

        // Add a listener to handle the response.




    }

}
