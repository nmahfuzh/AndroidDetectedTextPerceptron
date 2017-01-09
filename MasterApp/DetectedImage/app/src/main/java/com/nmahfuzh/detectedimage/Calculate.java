package com.nmahfuzh.detectedimage;

import android.util.Log;

/**
 * Created by mahfuzh on 28/09/2016.
 */

class Calculate {


    static String calc(String input) {
        String txtresult;

        String [] arrayKarakter = input.split(" ");
        double tmpResult = Double.valueOf(arrayKarakter[0]);
        for(int i=1;i<arrayKarakter.length;i++) {

            if(arrayKarakter[i].equals("+")){
                tmpResult=tmpResult+Double.valueOf(arrayKarakter[i+1]);
                i=+1;
            }else if(arrayKarakter[i].equals("-")){
                tmpResult=tmpResult-Double.valueOf(arrayKarakter[i+1]);
                i=+1;
            }else if(arrayKarakter[i].equals("x")){
                tmpResult=tmpResult*Double.valueOf(arrayKarakter[i+1]);
                i=+1;
            }else if(arrayKarakter[i].equals(":")){
                tmpResult=tmpResult/Double.valueOf(arrayKarakter[i+1]);
                i=+1;
            }
        }
        txtresult = String.valueOf(tmpResult);
        return txtresult;
    }

}
