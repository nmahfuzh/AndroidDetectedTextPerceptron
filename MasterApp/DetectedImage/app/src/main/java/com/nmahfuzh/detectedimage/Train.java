package com.nmahfuzh.detectedimage;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

class Train {
    private static String infoStat = "";

    static String ShowInf(){
        return infoStat;
    }

    static void LoadImageConvertArray() {
        infoStat = "";
        File DirFile [] = new File(MainActivity.strImage).listFiles();
        String xmlStr = "";
        for(File dir : DirFile){
            if(dir.isDirectory()){
                String nameDir = dir.getName();
                if(!(dir.getName().equals("tmp"))){
                    File imgFile[]= new File(MainActivity.strImage+"/"+nameDir).listFiles();
                    for (File anImgFile : imgFile) {
                        if(getFileExt(anImgFile.getName()).equalsIgnoreCase("jpg")){
                            xmlStr += nameDir;
                            //ConvertImage
                            Bitmap bmp = BitmapFactory.decodeFile(anImgFile.getPath());
                            Mat srcMat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC3);
                            Bitmap myBitmap32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
                            Utils.bitmapToMat(myBitmap32, srcMat);
                            Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGB2GRAY);
                            Size sizeImg = srcMat.size();

                            for (int x = 0; x < sizeImg.height; x++) {
                                for (int y = 0; y < sizeImg.width; y++) {
                                    double[] pix = srcMat.get(x, y);
                                    if (pix[0] > 200)
                                        xmlStr += ","+-1;
                                    else
                                        xmlStr += ","+1;
                                }
                            }
                            xmlStr += "\n";
                        }
                    }
                }
            }
        }
        infoStat +="\nSave File";
        File TrainFile = new File(new File(MainActivity.strImage), "TrainData.txt");
        if(TrainFile.exists())
            TrainFile.delete();
        try{
            FileOutputStream fos = new FileOutputStream(TrainFile);
            fos.write(xmlStr.getBytes());
            fos.flush();
            fos.close();
            infoStat +="\nSave Data Success";
        }catch (IOException ioe) {
            ioe.printStackTrace();
            infoStat+= String.valueOf(ioe);
        }
    }

    private static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    static double[][] ReadTrainData(double[][] data){
        infoStat = "";
        try{
            File file = new File(MainActivity.strImage+"/TrainData.txt");
            FileReader fileReader = new FileReader(file);
            FileReader fileReader1 = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
            String line;
            String [] lineData;
            int i=0;
            int lineLenght = 0;
            while(bufferedReader1.readLine()!=null){
                lineLenght++;
            }
            infoStat+="\nTotal Data Training : "+lineLenght;
            fileReader1.close();
            data = new double[lineLenght][0];
            while ((line = bufferedReader.readLine()) != null) {
                lineData = line.split(",");
                data[i] = new double[lineData.length];
                data[i]=StrtoDouble(lineData).clone();
                i++;
            }
            fileReader.close();
            infoStat+="\nRead Data Success";
        }catch (IOException e){
            e.printStackTrace();
            infoStat+=String.valueOf(e);
        }
        return data;
    }

    private static double[] StrtoDouble(String[] str){
        double [] tmp = new double[str.length] ;
        for(int i=0;i<tmp.length;i++){
            tmp[i] = Double.valueOf(str[i]);
        }
        return tmp;
    }


    static void LearnigPerceptron(double[][] input, int TotEpoch, double treshold, double learnrate){
        //Log.w("Lenght Data",String.valueOf(input[0].length));
        final int [][] target ={{-1,-1,-1,-1},{-1,-1,-1,1},{-1,-1,1,-1},{-1,-1,1,1},{-1,1,-1,-1},{-1,1,-1,1},{-1,1,1,-1},{-1,1,1,1},{1,-1,-1,-1},{1,-1,-1,1},{1,-1,1,-1},{1,-1,1,1},{1,1,-1,-1},{1,1,-1,1}} ;
        double [][] oldW = new double [4][input[0].length-1];
        double [][]newW = new double[4][input[0].length-1];
        double []oldB = {0,0,0,0};
        int [] FNet = {0,0,0,0};
        double []newB = new double[4];
        double tmpNet;
        int allTarget;
        int Epoch;
        Epoch = 0;

        //Set data old W zero
        for (int i=0;i<oldW.length;i++){
            for(int j=0;j<oldW[i].length;j++){
                oldW[i][j] = 0.0;
            }
        }

        do {
            //Log.w("TRAINING","Epoch : "+Epoch);
            allTarget = 0;
            for (double[] aDataIn : input) {
                int posNum = (int) aDataIn[0];
                //Log.w("TRAINING","DATA : "+posNum);
                for (int j = 0; j < target[posNum].length; j++) {
                    int aTarget = target[posNum][j];
                    tmpNet = calculateNet(aDataIn,oldW[j],oldB[j]);
                    FNet[j] = activationNet(tmpNet, treshold);
                    newW[j] = calculateNewW(aDataIn,aTarget,FNet[j],learnrate,oldW[j]).clone();
                    newB[j] = calculateNewB(oldB[j],learnrate,aTarget,FNet[j]);
                }
                oldW=newW.clone();
                oldB=newB.clone();
                if(Arrays.equals(FNet, target[posNum])){
                    allTarget++;
                }
            }
            if(allTarget >= input.length){
                infoStat+="\nData Has Know at Epoch : "+Epoch;
                //Epoch = TotEpoch;
            }
            Epoch++;
        }while (Epoch < TotEpoch);
        //simpan bobot
        infoStat+="\nSaving Weight";
        SimpanBobot(oldW,oldB);
        infoStat+="\nProcess Learning Finish";
    }

    private static int activationNet(double inputNet, double inputTresh){
        int result = 0;
        if(inputNet > inputTresh)
            result = 1;
        else if(inputNet == inputTresh)
            result = 0;
        else if(inputNet < inputTresh)
            result = -1;
        return result;
    }

    private static double calculateNet(double[] inputX, double[] W, double b){
        double Net = 0;
        for(int i=0;i<W.length;i++){
            Net += inputX[i+1] * W[i];
        }
        Net+=b;
        //DecimalFormat decimalFormat = new DecimalFormat("#.#####");
        //Net = Double.valueOf(decimalFormat.format(Net));
        return Net;
    }

    private static double calculateNewB(double inputB, double inputLearn, int inputTarget, int inputFnet){
        double newB;
        if(inputFnet!=inputTarget)
            newB = inputB + inputLearn*inputTarget;
        else
            newB = inputB;
        //DecimalFormat decimalFormat = new DecimalFormat("#.#####");
        //newB = Double.valueOf(decimalFormat.format(newB));
        return newB;
    }

    private static double [] calculateNewW(double[] input, int inputTarget, int inputFnet, double inputLearn, double[] inputWold){
        double []newW = new double[inputWold.length];
        for(int i=0;i<inputWold.length;i++){
            if(inputFnet != inputTarget)
                newW[i] = inputWold[i] + inputLearn*inputTarget*input[i+1];
            else
                newW[i] = inputWold[i];
            //DecimalFormat decimalFormat = new DecimalFormat("#.#####");
            //newW[i] = Double.valueOf(decimalFormat.format(newW[i]));
        }
        return newW;
    }

    private static void SimpanBobot(double[][] BobotW, double[] bobotB){
        String dataStr = "";

        for(int i=0;i<BobotW.length;i++){
            dataStr+=String.valueOf(bobotB[i]);
            for(int j=0;j<BobotW[i].length;j++){
                dataStr+=","+String.valueOf(BobotW[i][j]);
            }
            dataStr+="\n";
        }

        File bobotFile = new File(new File(MainActivity.strImage),"BobotFile.txt");
        if(bobotFile.exists())
            //noinspection ResultOfMethodCallIgnored
            bobotFile.delete();
        try{
            FileOutputStream fos = new FileOutputStream(bobotFile);
            fos.write(dataStr.getBytes());
            fos.flush();
            fos.close();
            infoStat +="\nSave Data Success";
        }catch (IOException ioe){
            ioe.printStackTrace();
            infoStat+="\nError on Saving Weght";
        }
    }
}
