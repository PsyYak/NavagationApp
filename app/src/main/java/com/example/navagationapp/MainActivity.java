package com.example.navagationapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.navagationapp.Utils.FileUtil;
import com.example.navagationapp.Utils.LoadingBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    // global vars
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1234;
    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;


    // file vars
    private static Cell cell;
    private static Sheet sheet;

    // vars
    LoadingBar loadingBar;
    private Handler handler = new Handler();



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
                        WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
                            // show the user a dialog of progress in new thread
                            loadingBar = new LoadingBar(MainActivity.this);
                            loadingBar.showDialog();

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
                                        loadingBar.dismissDialog();
                                        Log.d(TAG, "onActivityResult: Not supported file type, notify user.");
                                        Toast.makeText(MainActivity.this,"File extension is not supported, please use either Xlsx or Xls.",Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            } catch (IOException e) {
                                loadingBar.dismissDialog();
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
    private void xlsFilePrint(FileInputStream fileInputStream){
        try {
            Workbook workbook = new HSSFWorkbook(fileInputStream);
            sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            StringBuilder sb = new StringBuilder();

            // outer loop, loops through rows
            for (int r = 0; r < rowsCount; r++) {

                Row row = sheet.getRow(r);
                int cellCount = row.getPhysicalNumberOfCells();
                // inner loop, loops through columns
                for (int c = 0; c < cellCount; c++) {

                    String value = getCellAsString(row,c,formulaEvaluator);
                    String cellInfo = "r:"+r+"; c:"+c+"; value:"+value;
                    Log.d(TAG, "xlsFilePrint: Data from row: "+cellInfo);
                    sb.append(value).append(",");


                }
                sb.append(";");
            }
            Log.d(TAG, "xlsFilePrint: StringBuilder data: "+ sb.toString());
            parseStringBuilder(sb);

        } catch (IOException e) {
            loadingBar.dismissDialog();
            e.printStackTrace();
        }

    }

    private void parseStringBuilder(StringBuilder mStringBuilder) {
        Log.d(TAG, "parseStringBuilder: Started parsing");

        String[] rows = mStringBuilder.toString().split(";");
        for(int i=0;i<rows.length;i++){
            String[] columns = rows[i].split(",");

            try{
                String value1 = columns[0];
                String value2 = columns[1];
                String value3 = columns[2];
                String value4 = columns[3];
                String value5 = columns[4];
                String value6 = columns[5];



                String cellInfo = "value1:"+value1+",value2:"+value2+"value3:"+value3+",value4:"+value4+"value5:"+value5+",value6:"+value6;
                Log.d(TAG, "parseStringBuilder: cellInfo: "+cellInfo);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadingBar.dismissDialog();
                        Log.d(TAG, "run: File parsed successfully");
                    }
                },2000);

            }catch (NullPointerException e){
                //loadingBar.dismissDialog();
                Log.d(TAG, "parseStringBuilder: NPE : "+e.getMessage());
            }

        }



    }

    private String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        Log.d(TAG, "getCellAsString: getting cell value");
        String value="";
        CellValue cellValue = null;
        try {
            Cell cell = row.getCell(c);
             cellValue = formulaEvaluator.evaluate(cell);
            switch (cellValue.getCellType()){

                case Cell.CELL_TYPE_BOOLEAN:
                    value=""+cellValue.getBooleanValue();
                    break;

                case Cell.CELL_TYPE_NUMERIC:
                    double numericValue = cellValue.getNumberValue();
                    if(HSSFDateUtil.isCellDateFormatted(cell)){
                        double date = cellValue.getNumberValue();
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.dateFormat));
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    }else{
                        value=""+numericValue;
                    }
                    break;

                case Cell.CELL_TYPE_STRING:
                    value = ""+cellValue.getStringValue();
                    break;

                default:

            }
        }catch (NullPointerException e){
            //loadingBar.dismissDialog();
            Log.d(TAG, "getCellAsString: NPE: "+e.getMessage());
        }


    return value;
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
                      //  System.out.println(cell.getNumericCellValue());
                        break;

                    case Cell.CELL_TYPE_STRING:
                       // System.out.println(cell.getStringCellValue());
                        break;
                }
                System.out.println("\t");

            }

        }
    }


}