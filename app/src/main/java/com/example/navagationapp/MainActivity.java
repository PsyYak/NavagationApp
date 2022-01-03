package com.example.navagationapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.navagationapp.Utils.FileUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
                        pickFileToUpload();
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
                    pickFileToUpload();
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

    private void pickFileToUpload(){

        Intent pickFile = new Intent(Intent.ACTION_GET_CONTENT);
        String[] mimeTypes = {"application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
        pickFile.addCategory(Intent.CATEGORY_OPENABLE);
        pickFile.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        StringBuilder mimeTypesStr = new StringBuilder();
        for (String mimeType : mimeTypes) {
            mimeTypesStr.append(mimeType).append("|");
        }
        pickFile.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));

        pickFileActivity.launch(pickFile);



    }
    ActivityResultLauncher<Intent> pickFileActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        Intent data = result.getData();
                        if (data != null) {
                            // convert intent data to uri data
                            Uri uri = data.getData();
                            // getting the file extension
                            String fileExtension = FileUtil.getFileExt(getApplicationContext(),uri);
                            // creating temp file with the file name "test" in app dir
                            String realFileName = FileUtil.createCopyAndReturnRealPath(getApplicationContext(),uri,"test");
                            // assign that file to myFile
                            File myFile = new File(Objects.requireNonNull(realFileName));

                            // getting the file we created above
                            File pickedFile = new File(getFilesDir(),myFile.getName());
                            FileInputStream fileInputStream = null;
                            try {
                                fileInputStream = new FileInputStream(pickedFile);
                                // checking file extension
                                if (fileExtension != null) {
                                    if(fileExtension.contains("xls")){
                                        Log.d(TAG, "onActivityResult: Got xls.");
                                        xlsFilePrint(fileInputStream);
                                    }else if(fileExtension.contains("xlsx")){
                                        Log.d(TAG, "onActivityResult: Got xlsx.");
                                        xlsxFilePrint(fileInputStream);
                                    }else{
                                        Log.d(TAG, "onActivityResult: Not supported file type, notify user.");
                                        Toast.makeText(MainActivity.this,"File extension is not supported, please use either Xlsx or Xls.",Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }else{
                            Toast.makeText(MainActivity.this,"Couldn't upload file, try again.",Toast.LENGTH_LONG)
                                    .show();

                        }

                    }
                }
            });


    // handle xls file type
    private void xlsFilePrint(FileInputStream fileInputStream) throws IOException {
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
                System.out.println("\t");

            }

        }
    }

    // handle xlsx file type
    private void xlsxFilePrint(FileInputStream fileInputStream) throws IOException {

        XSSFWorkbook myWorkBook = new XSSFWorkbook(fileInputStream);
        sheet = myWorkBook.getSheetAt(0);
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
                System.out.println("\t");

            }

        }
    }


}