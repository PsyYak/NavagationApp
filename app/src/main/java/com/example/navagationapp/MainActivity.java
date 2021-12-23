package com.example.navagationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isServicesOk()){
            init();
        }



    }

    private void init(){
        Button btnMap = findViewById(R.id.btnMap);
        btnMap.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,MapActivity.class);
            startActivity(intent);
        });

    }
    public boolean isServicesOk(){
        Log.d(TAG,"Checking services is ok");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            // all ok
            Log.d(TAG, "Google play services up to date");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
             // error that we can use google dialog to fix
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this,available,ERROR_DIALOG_REQUEST);
            if (dialog != null) {
                dialog.show();
            }
        }else{
            Toast.makeText(MainActivity.this, "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        // return false if we cant handle error
        return false;

    }
}