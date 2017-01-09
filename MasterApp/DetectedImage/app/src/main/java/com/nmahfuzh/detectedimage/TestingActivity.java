package com.nmahfuzh.detectedimage;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestingActivity extends AppCompatActivity{

    private static final int RESULT_CAM = 0;
    private static final int RESULT_LOAD_IMAGE = 1;
    ImageView imgView;
    Bitmap bmp;
    String txtResult = "";
    TextView resultTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        imgView = (ImageView) findViewById(R.id.image_View);
        resultTextView = (TextView)findViewById(R.id.textResult);
    }

    public void useCam(View v){
        clearImg();
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, RESULT_CAM);
    }

    public void loadImage(View v){
        clearImg();
        txtResult = "";
        Intent intLoad = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intLoad, RESULT_LOAD_IMAGE);
    }

    public void testData(View v){
        if (imgView.getDrawable() != null) {
            BitmapDrawable abmp = (BitmapDrawable) imgView.getDrawable();
            bmp = abmp.getBitmap();

            //Convert Bitmap to Mat
            Mat srcMat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
            Bitmap myBitmap32 = bmp.copy(Bitmap.Config.RGB_565, true);
            Utils.bitmapToMat(myBitmap32, srcMat);


            Mat cannyImage = pre_processing(srcMat,0);
            Mat cpImage = srcMat.clone();

            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(cannyImage, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            for (int i = 1; i < contours.size(); i++) {
                MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
                double approxDistance = Imgproc.arcLength(contour2f,true)*0.02;
                Imgproc.approxPolyDP(contour2f,approxCurve,approxDistance,true);
                MatOfPoint points = new MatOfPoint(approxCurve.toArray());
                Rect rect = Imgproc.boundingRect(points);

                if(rect.width > 8 && rect.height > 8){
                Point centerP = new Point(rect.x + (rect.width / 2), rect.y + (rect.height / 2));
                Point newPoint1 = null, newPoint2 = null;
                if (rect.width >= rect.height) {
                    newPoint1 = new Point(centerP.x - (rect.width) / 2, centerP.y - (rect.width) / 2);
                    newPoint2 = new Point(newPoint1.x + rect.width, newPoint1.y + rect.width);
                }
                if (rect.height >= rect.width) {
                    newPoint1 = new Point((centerP.x - (rect.height) / 2), (centerP.y - (rect.height) / 2));
                    newPoint2 = new Point(newPoint1.x + rect.height, newPoint1.y + rect.height);
                }

                if (newPoint1 != null && newPoint1.x > 1 && newPoint1.y > 1 && newPoint1.x < srcMat.width() && newPoint1.y < srcMat.height() && newPoint2.x > 1 && newPoint2.y > 1 && newPoint2.x < srcMat.width() && newPoint2.y < srcMat.height()) {

                    //Draw-reactangle
                    Imgproc.rectangle(srcMat, newPoint1, newPoint2, new Scalar(255, 0, 0), 1);

                    Rect cropRect = new Rect(newPoint1, newPoint2);
                    Mat cropImg = new Mat(cpImage, cropRect);
                    Mat resizeImg = new Mat(28,28,CvType.CV_8UC1);
                    Imgproc.resize(cropImg,resizeImg,resizeImg.size());
                    Mat cropTresh = pre_processing(resizeImg,1);

                    Imgcodecs.imwrite(MainActivity.strTmp + "/" + rect.x + ".jpg", cropTresh);

                }
                }
            }
            Bitmap resultBitmap = Bitmap.createBitmap(srcMat.cols(), srcMat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(srcMat, resultBitmap);
            imgView.setImageBitmap(resultBitmap);

            //File RootimgFile [] = new File(MainActivity.strTmp).listFiles();
            File imgFile[] = OrderMyFiles(new File(MainActivity.strTmp));
            if(imgFile != null){
                //List<File>sortImageFile = new ArrayList<File>();
                for(File fileimg : imgFile){
                    bmp = BitmapFactory.decodeFile(fileimg.getPath());
                    Mat tmpImage = new Mat(bmp.getHeight(),bmp.getWidth(),CvType.CV_8UC3);
                    myBitmap32 = bmp.copy(Bitmap.Config.RGB_565,true);
                    Utils.bitmapToMat(myBitmap32,tmpImage);
                    Mat tmpGrayImage = new Mat(tmpImage.size(),CvType.CV_8UC1);
                    Imgproc.cvtColor(tmpImage,tmpGrayImage,Imgproc.COLOR_RGB2GRAY,4);
                    txtResult+=Test.Perceptron(tmpGrayImage);
                }
            }
            txtResult+=" = "+Calculate.calc(String.valueOf(txtResult));

        } else {
            Toast.makeText(this, "Image Is Empty", Toast.LENGTH_LONG).show();
        }
        resultTextView.setText(txtResult);
    }

    File[] OrderMyFiles (File rootFolder){

        File[] listOfFiles = rootFolder.listFiles();
        Arrays.sort(listOfFiles, new SortFileName() );
        return listOfFiles;
    }

    public static Mat pre_processing(Mat imgSrc,int c){

        Mat grayImage = new Mat(imgSrc.size(), CvType.CV_8UC1);
        Mat erodeImage = new Mat(imgSrc.size(),CvType.CV_8UC1);
        Mat blurImage = new Mat(imgSrc.size(),CvType.CV_8UC1);
        Mat bwImage = new Mat(imgSrc.size(),CvType.CV_8UC1);
        Mat resultImage = new Mat(imgSrc.size(),CvType.CV_8UC1);

        if(c == 0){
            Imgproc.cvtColor(imgSrc, grayImage, Imgproc.COLOR_RGB2GRAY, 4);
            Imgproc.GaussianBlur(grayImage, blurImage, new Size(9, 9), 2, 2);
            //Imgproc.adaptiveThreshold(blurImage, blurImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);
            //Imgproc.dilate(blurImage, resultImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

            //Imgproc.GaussianBlur(grayImage, blurImage, new Size(5, 5),0,0);
            //Imgproc.erode(grayImage, erodeImage, Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3,3)));
            //double tresh_val = Imgproc.threshold(blurImage,bwImage,0,255,Imgproc.THRESH_OTSU);
            //Log.w("Teshold_Value : ",String.valueOf(tresh_val));
            //Imgproc.Canny(blurImage,resultImage,8,tresh_val);
            Imgproc.threshold(blurImage,resultImage,0,255,Imgproc.THRESH_OTSU);
        }else if(c == 1){
            Imgproc.cvtColor(imgSrc, grayImage, Imgproc.COLOR_RGB2GRAY, 4);
            Imgproc.GaussianBlur(grayImage, blurImage, new Size(9, 9), 2, 2);
            Imgproc.threshold(blurImage,resultImage,0,255,Imgproc.THRESH_OTSU);
            //Imgproc.adaptiveThreshold(blurImage, resultImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        }
        return resultImage;
    }

    public void clearImg() {
        File f = new File(MainActivity.strTmp);
        File fTmp[] = f.listFiles();
        for (File atmp : fTmp) {
            atmp.delete();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                Bitmap bitmapImage = BitmapFactory.decodeFile(picturePath);
                //int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
                //Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
                imgView.setImageBitmap(bitmapImage);
            }
        }
        if(requestCode == RESULT_CAM && resultCode == RESULT_OK && null != data){
            Bitmap bp = (Bitmap) data.getExtras().get("data");
            imgView.setImageBitmap(bp);
        }

    }
}