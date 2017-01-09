package com.nmahfuzh.detectedimage;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Test extends Service {

    public static double[][] Weight  = null;
    public static double[] WeightB  = null;

    public Test() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    public static void WeightData(){

        try{
            File FileWeight = new File(new File (MainActivity.strImage),"BobotFile.txt");
            FileReader fileReader = new FileReader(FileWeight);
            FileReader fileReader1 = new FileReader(FileWeight);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            BufferedReader bufferedReader1 = new BufferedReader(fileReader1);

            String line;
            String [] lineData;
            int i=0;
            int lineLenght = 0;

            while (bufferedReader.readLine()!=null){
                lineLenght++;
            }
            fileReader.close();
            Weight = new double[lineLenght][0];
            WeightB = new double[lineLenght];
            while ((line = bufferedReader1.readLine())!=null){
                lineData=line.split(",");
                Weight[i] = new double[lineData.length-1];
                WeightB[i] = Double.valueOf(lineData[0]);
                Weight[i] = StrtoDouble(lineData).clone();
                i++;
            }
            fileReader1.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double[] StrtoDouble(String[] str){
        double [] tmp = new double[str.length-1] ;
        for(int i=1;i<tmp.length;i++){
            tmp[i] = Double.valueOf(str[i]);
        }
        return tmp;
    }

    public static String Perceptron (Mat input){
        final int [][] target ={{-1,-1,-1,-1},{-1,-1,-1,1},{-1,-1,1,-1},{-1,-1,1,1},{-1,1,-1,-1},{-1,1,-1,1},{-1,1,1,-1},{-1,1,1,1},{1,-1,-1,-1},{1,-1,-1,1},{1,-1,1,-1},{1,-1,1,1},{1,1,-1,-1},{1,1,-1,1}} ;
        char [] strChar = {'0','1','2','3','4','5','6','7','8','9','x',':','-','+'};
        String resultChar = "";
        double dataInput [] = new double[input.height()*input.width()];
        WeightData();
        double [] Net= new double[WeightB.length];
        int [] Fnet = new int[4];
        final double treshold = 0.05;
        int indexData = 0;

        for (int x = 0; x < input.height(); x++) {
            for (int y = 0; y < input.width(); y++) {
                double[] pix = input.get(x, y);
                if (pix[0] > 200)
                    dataInput[indexData] = -1;
                else
                    dataInput[indexData]=1;
                indexData++;
            }
        }

        for(int i=0;i<WeightB.length;i++){
            Net[i] = WeightB[i];
            for (int j=0;j<Weight[i].length;j++){
                Net[i]+=dataInput[j]*Weight[i][j];
            }

            if(Net[i] > treshold)
                Fnet[i] = 1;
            else if(Net[i] == treshold)
                Fnet[i] = 0;
            else if(Net[i] < treshold)
                Fnet[i] = -1;
        }

        for(int x=0;x<target.length;x++){
            if(Arrays.equals(target[x],Fnet)){
                if(strChar[x] == '-' || strChar[x] == '+' || strChar[x] == ':' || strChar[x] == 'x'){
                    resultChar+= " "+strChar[x]+" ";
                }else{
                    resultChar+= strChar[x];
                }
            }
        }

        return resultChar;
    }
}
