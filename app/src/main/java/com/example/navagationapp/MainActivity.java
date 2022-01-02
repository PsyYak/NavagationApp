package com.example.navagationapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String MANAGE_EXTERNAL_STORAGE = Manifest.permission.MANAGE_EXTERNAL_STORAGE;


    // file vars
    private static Cell cell;
    private static Sheet sheet;
    private Workbook workbook = new HSSFWorkbook();

    // global vars
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1234;
    private static final int PICKFILE_RESULT_CODE = 4444;


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

        Button btnImport = findViewById(R.id.btnImport);
        btnImport.setOnClickListener(imp -> {
            // choose xls and/or xlsx
             storagePermissionOk();
        });

    }

    private void storagePermissionOk(){
        Log.d("storagePermissionOk", "getting storage permission checked");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            String [] permission = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE};
            if(ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(),
                    READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(),
                        MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // open file picker

                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    // Define file types to xls and xlsx
                    String[] mimeTypes = {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
                    // Ask specifically for something that can be opened:
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        chooseFile.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
                        if (mimeTypes.length > 0) {
                            chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                        }
                    } else {
                        String mimeTypesStr = "";
                        for (String mimeType : mimeTypes) {
                            mimeTypesStr += mimeType + "|";
                        }
                        chooseFile.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
                    }

                    startActivityForResult(
                            Intent.createChooser(chooseFile, "Choose a file"),
                            PICKFILE_RESULT_CODE
                    );
                }else{
                    ActivityCompat.requestPermissions(MainActivity.this,permission,STORAGE_PERMISSION_REQUEST_CODE);
                }

            }else{
                ActivityCompat.requestPermissions(MainActivity.this,permission,STORAGE_PERMISSION_REQUEST_CODE);
            }
        }else{
            String [] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
            if(ContextCompat.checkSelfPermission(MainActivity.this.getApplicationContext(),
                    READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // open file picker
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                // Define file types to xls and xlsx
                String[] mimeTypes = {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
               // Ask specifically for something that can be opened:
                chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    chooseFile.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
                    if (mimeTypes.length > 0) {
                        chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    }
                } else {
                    String mimeTypesStr = "";
                    for (String mimeType : mimeTypes) {
                        mimeTypesStr += mimeType + "|";
                    }
                    chooseFile.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
                }
                startActivityForResult(
                        Intent.createChooser(chooseFile, "Choose a file"),
                        PICKFILE_RESULT_CODE
                );
            }else{
                ActivityCompat.requestPermissions(this,permission,STORAGE_PERMISSION_REQUEST_CODE);
            }
        }



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
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
            if (dialog != null) {
                dialog.show();
            }
        }else{
            Toast.makeText(MainActivity.this, "You can't make map request", Toast.LENGTH_SHORT).show();
        }
        // return false if we cant handle error
        return false;

    }
    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result)  {
        String displayName="empty";
        if(resultCode == RESULT_OK) {
            if (requestCode == PICKFILE_RESULT_CODE ) {
                if (result != null){
                Log.d(TAG, "onActivityResult: Proceed to get data from result");
                    Uri uri = result.getData();
                    String uriString = uri.toString();
                    File myFile = new File(uriString);
                    String path = myFile.getAbsolutePath();
                    Log.d(TAG, "onActivityResult: path is:"+path);
                    if (uriString.startsWith("content://")) {
                        Cursor cursor = null;
                        try {
                            cursor = MainActivity.this.getContentResolver().query(uri, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                Log.d(TAG, "onActivityResult: cursor place, displayName is:"+displayName);
                                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                Log.d(TAG, "onActivityResult: display name is now : "+displayName);
                            }
                        } finally {
                            cursor.close();
                        }
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.getName();
                    }
                    Log.d(TAG, "onActivityResult: displayName is: "+displayName);
                    String realFileName = displayName;

                try {
                    Log.d(TAG, "onActivityResult: path is:"+path);
                    File file = new File(MainActivity.this.getExternalFilesDir(null),realFileName);
                    FileInputStream fileInputStream = null;
                    fileInputStream = new FileInputStream(file);
                    workbook = new HSSFWorkbook(fileInputStream);
                    sheet = workbook.getSheetAt(0);
                    for (Row row : sheet) {
                        Iterator<Cell> cellIterator = row.cellIterator();
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();

                            // Check cell type and format accordingly
                            switch (cell.getCellType()) {
                                case Cell.CELL_TYPE_NUMERIC:
                                    // Print cell value
                                    System.out.println(cell.getNumericCellValue());
                                    break;

                                case Cell.CELL_TYPE_STRING:
                                    System.out.println(cell.getStringCellValue());
                                    break;
                            }


                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                }else{
                    Log.d(TAG, "onActivityResult: fail picking/upload error");
                    Toast.makeText(MainActivity.this,"Error on picking file, try again later",Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }else{
            Log.d(TAG, "onActivityResult: result code error: "+resultCode);
        }


        super.onActivityResult(requestCode, resultCode, result);
    }



}