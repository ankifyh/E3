package com.mj.e3;

import android.app.Activity;
import android.app.Service;
import android.os.Vibrator;

class VibrateUtil {
    //震动milliseconds毫秒
     static void vibrate(final Activity activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        assert vib != null;
        vib.vibrate(milliseconds);
    }
}
