package com.nmahfuzh.detectedimage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class TrainingData extends AppCompatActivity {

    String [] infoProcess;
    String stage = "";
    TextView textResult;
    EditText editEpoch,editTreshold,editLearnRate;

    double [][] datain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_data);

        textResult = (TextView)findViewById(R.id.text_result);
        textResult.setMovementMethod(new ScrollingMovementMethod());
        editEpoch = (EditText)findViewById(R.id.edit_epoch);
        editLearnRate = (EditText)findViewById(R.id.edit_learn_rate);
        editTreshold = (EditText)findViewById(R.id.edit_treshold);

    }

    public void ConvertDataImage(View v){
        infoProcess = getResources().getStringArray(R.array.InfoConvertData);
        infoProcess [3] +=MainActivity.strImage;
        new CountDownTimer(infoProcess.length*1000,1000){
            int infoIndex = 0;
            @Override
            public void onTick(long millisUntilFinished) {
                textResult.setText(stage+= String.format("\n%s", infoProcess[infoIndex]));
                infoIndex++;
            }

            @Override
            public void onFinish() {
                Train.LoadImageConvertArray();
                textResult.setText(stage+=Train.ShowInf());
            }
        }.start();
    }

    public void ProcesTrain(View v){
        final int setEpoch = Integer.valueOf(String.valueOf(editEpoch.getText()));
        final double setTreshold = Double.valueOf(String.valueOf(editTreshold.getText()));
        final double setLearnRate = Double.valueOf(String.valueOf(editLearnRate.getText()));

        if((setTreshold <= 0.00 || setLearnRate <= 0.00 || setEpoch <= 0)){
            Toast.makeText(this,"Opetion on Treshold or Learn Rate must Not 0.00",Toast.LENGTH_LONG).show();
        }else{
            infoProcess = getResources().getStringArray(R.array.InfoReadData);
            infoProcess [0] +=MainActivity.strImage+"/TrainData.txt";
            new CountDownTimer(infoProcess.length*1000,1000){
                int infoIndex = 0;
                @Override
                public void onTick(long millisUntilFinished) {
                    textResult.setText(stage+= String.format("\n%s", infoProcess[infoIndex]));
                    infoIndex++;
                }

                @Override
                public void onFinish() {
                    datain = Train.ReadTrainData(datain).clone();
                    textResult.setText(stage+=Train.ShowInf());

                    infoProcess = getResources().getStringArray(R.array.InfoTrainData);
                    infoProcess [2] +=setEpoch;
                    infoProcess [4] +=setTreshold;
                    infoProcess [5] +=setLearnRate;
                    new CountDownTimer(infoProcess.length*1000,1000){
                        int infoIndex = 0;
                        @Override
                        public void onTick(long millisUntilFinished) {
                            textResult.setText(stage+= String.format("\n%s", infoProcess[infoIndex]));
                            infoIndex++;
                        }

                        @Override
                        public void onFinish() {
                            Train.LearnigPerceptron(Train.ReadTrainData(datain),setEpoch,setTreshold,setLearnRate);
                            textResult.setText(stage+=Train.ShowInf());
                        }
                    }.start();
                }
            }.start();
        }
    }
}
