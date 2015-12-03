package motocitizen.utils;

import android.content.res.TypedArray;
import android.os.Build;
import android.util.DisplayMetrics;

import java.text.SimpleDateFormat;
import java.util.Locale;

import motocitizen.MyApp;

public class Const {
    public static final SimpleDateFormat FULL_TIME_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault());
    public static final SimpleDateFormat DATE_FORMAT      = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    public static final SimpleDateFormat TIME_FORMAT      = new SimpleDateFormat("HH:mm", Locale.getDefault());
    public final static int              EQUATOR          = 20038;
    public final static String           PHONE            = "78007751734";

    // Defines a custom Intent action
    public static final String BROADCAST_ACTION = "motocitizen.BROADCAST";

    @SuppressWarnings("deprecation")
    public static int getWidth() {
        if (Build.VERSION.SDK_INT < 13) {
            return MyApp.getCurrentActivity().getWindowManager().getDefaultDisplay().getWidth();
        } else {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            MyApp.getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            return displaymetrics.widthPixels;
        }
    }

    //TODO изничтожить
    public static int getDefaultBGColor() {
        TypedArray ta    = MyApp.getCurrentActivity().obtainStyledAttributes(new int[]{android.R.attr.colorBackground, android.R.attr.textColorPrimary});
        int        color = ta.getIndex(0);
        ta.recycle();
        return color;
    }
}
