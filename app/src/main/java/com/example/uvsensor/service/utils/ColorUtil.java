package com.example.uvsensor.service.utils;

import android.graphics.Color;
import android.location.LocationManager;
import android.util.Log;

public class ColorUtil {

    // 调色网站在：https://photokit.com/colors/color-gradient/?lang=zh
    // 从1到4精度逐渐降低，颜色逐渐变浅

    // GPS是蓝色系
    public static int GPS_1 = Color.parseColor("#0ebeff");
    public static int GPS_2 = Color.parseColor("#44ccff");
    public static int GPS_3 = Color.parseColor("#79dbff");
    public static int GPS_4 = Color.parseColor("#afe9ff");

    // Network是绿色系
    public static int NETWORK_1 = Color.parseColor("#0f4814");
    public static int NETWORK_2 = Color.parseColor("#2a5c2e");
    public static int NETWORK_3 = Color.parseColor("#447148");
    public static int NETWORK_4 = Color.parseColor("#5f8562");


    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    // 我感觉对于校园和城中村来说，accuracy大于30其实就没什么意义了
    public static int getColorByAccuracy(float accuracy, String src) {

        if (src.equals(LocationManager.GPS_PROVIDER)) {  // 千万别用等号啊！！不然会比较两个字符串的地址！！
            if (accuracy <= 3) return GPS_1;
            if (accuracy <= 7) return GPS_2;
            if (accuracy <= 30) return GPS_3;
            return GPS_4;
        }
        else if (src.equals(LocationManager.NETWORK_PROVIDER)) {
            if (accuracy <= 3) return NETWORK_1;
            if (accuracy <= 7) return NETWORK_2;
            if (accuracy <= 30) return NETWORK_3;
            return NETWORK_4;
        }

        return 0;  // 这行应该不会走到
    }

    public static float getAccuracyByColor(int color) {
        // 我们返回平均值
        if (color == GPS_1 || color == NETWORK_1) {
            return 1.5F;
        } else if (color == GPS_2 || color == NETWORK_2) {
            return 3.5F;
        } else if (color == GPS_3 || color == NETWORK_3) {
            return 15.0F;
        } else if (color == GPS_4 || color == NETWORK_4) {
            return 30F;
        }
        return 0;  // 这样应该也走不到
    }
}
