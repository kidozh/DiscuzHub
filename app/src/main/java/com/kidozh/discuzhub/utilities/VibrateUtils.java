package com.kidozh.discuzhub.utilities;

import android.app.Service;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import com.kidozh.discuzhub.R;

import es.dmoral.toasty.Toasty;

public class VibrateUtils {
    public static void vibrateForNotice(Context context){
        Vibrator vb = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        int VIBRATING_MILSEC = 200;
        if(vb!=null&&vb.hasVibrator()){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = null;
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
                    vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
                }
                else {
                    vibrationEffect = VibrationEffect.createOneShot(VIBRATING_MILSEC,5);
                }


                vb.vibrate(vibrationEffect);
            }


        }

    }

    public static void vibrateSlightly(Context context){
        Vibrator vb = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        int VIBRATING_MILSEC = 200;
        if(vb!=null&&vb.hasVibrator()){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = null;
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){
                    vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
                }
                else {
                    vibrationEffect = VibrationEffect.createOneShot(VIBRATING_MILSEC,10);
                }
                vb.vibrate(vibrationEffect);
            }
            else {
                vb.vibrate(VIBRATING_MILSEC);
            }

        }
        else {
            Toasty.warning(context,context.getString(R.string.vibrator_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    public static void vibrateForError(Context context){
        Vibrator vb = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        int VIBRATING_MILSEC = 200;
        if(vb!=null&&vb.hasVibrator()){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                VibrationEffect vibrationEffect = null;
                vibrationEffect = VibrationEffect.createOneShot(VIBRATING_MILSEC,100);
                vb.vibrate(vibrationEffect);
            }
            else {
                vb.vibrate(VIBRATING_MILSEC);
            }

        }
        else {
            Toasty.warning(context,context.getString(R.string.vibrator_not_found), Toast.LENGTH_SHORT).show();
        }
    }
}
