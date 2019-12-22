package com.kidozh.discuzhub.utilities;

import java.util.Locale;

public class numberFormatUtils {
    public static String getShortNumberText(String number){
        try{
            int numberInt = Integer.parseInt(number);
            return getShortNumberText(numberInt);
        }
        catch (Exception e){
            e.printStackTrace();
            return number;
        }

    }

    public static String getShortNumberText(int number){
        int floatNumber = (int) number;
        int absNumber = Math.abs(number);
        if(absNumber > 1000*1000*1000){
            return String.format(Locale.getDefault(),"%.2f B",(float) floatNumber / (1000*1000*1000));
        }
        else if(absNumber > 1000*1000){
            return String.format(Locale.getDefault(),"%.2f M",(float) floatNumber / (1000*1000));
        }
        if(absNumber > 1000){
            return String.format(Locale.getDefault(),"%.2f K",(float) floatNumber / 1000);
        }
        else {
            return String.format(Locale.getDefault(),"%d",floatNumber);
        }

    }
}
