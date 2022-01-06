package com.example.navagationapp.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.navagationapp.R;

public class LoadingBar {

    Activity activity;
    AlertDialog dialog;


    public LoadingBar(Activity activity){
        this.activity = activity;
    }


    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.progress_layout,null));
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();

    }

    public void dismissDialog(){
        if(dialog!=null && dialog.isShowing())
        dialog.dismiss();
    }
}
