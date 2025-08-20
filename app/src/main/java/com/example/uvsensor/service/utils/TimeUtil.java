package com.example.uvsensor.service.utils;

public class TimeUtil {

    // 并且记得改成一个静态函数
    public static String mill2Time(long mill){  // 此函数将毫秒转换为我们常用的 分钟:秒 的形式
        long second = mill / 1000;
        long minute = second / 60;
        long last_second = second % 60;
        String strSecond, strMinute;

        if(last_second < 10){
            strSecond = "0" + last_second;
        }else{
            strSecond = last_second + "";  // 在后面加一个空的字符串转化为String
        }

        if(minute < 10){
            strMinute = "0" + minute;
        }else{
            strMinute = minute + "";  // 在后面加一个空的字符串转化为String
        }

        return strMinute + ":" + strSecond;
    }
}
