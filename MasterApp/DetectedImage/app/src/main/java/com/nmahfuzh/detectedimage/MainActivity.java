package com.nmahfuzh.detectedimage;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.opencv.android.OpenCVLoader;

import java.io.File;

public class MainActivity extends AppCompatActivity{

    public static String strImage = Environment.getExternalStorageDirectory()+"/nmApp";
    public static String strTmp = Environment.getExternalStorageDirectory()+"/nmApp/tmp";

    //OpenCV
    static {
        if(!OpenCVLoader.initDebug()){
            Log.i("OpenCV", "OpenCV Initialization failed");
        }else{
            Log.i("OpenCV","OpenCV initialization Success");
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check for permission
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }

        //CreateDirectoryAnd Check
        File AppDir = new File(strImage);
        File Dirtmp = new File(strTmp);
        if(!AppDir.isDirectory() && !AppDir.exists()){
            AppDir.mkdir();
            if(!Dirtmp.isDirectory() && !Dirtmp.exists())
                Dirtmp.mkdir();
        }
    }

    public void goToTest(View view){
        Intent intTest = new Intent(this,TestingActivity.class);
        startActivity(intTest);
    }
    public void goToTrain(View view){
        Intent intTest = new Intent(this,TrainingData.class);
        startActivity(intTest);
    }
}
