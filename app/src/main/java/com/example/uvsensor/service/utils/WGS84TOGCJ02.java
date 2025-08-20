package com.example.uvsensor.service.utils;

public class WGS84TOGCJ02 {
    private static final double PI = 3.14159265358979323846;

    public static double[] wgs84ToGcj02(double lat, double lon) {
//        if (isOutOfChina(lat, lon)) {
//            return new double[]{lat, lon};
//        } else {
//            double dLat = transformLat(lon - 105.0, lat - 35.0);
//            double dLon = transformLon(lon - 105.0, lat - 35.0);
//            double radLat = lat / 180.0 * PI;
//            double magic = Math.sin(radLat);
//            magic = 1 - 0.00669342162296595 * magic * magic;
//            double sqrtMagic = Math.sqrt(magic);
//            dLat = (dLat * 180.0) / ((6335552.727 / (magic * sqrtMagic)) * PI);
//            dLon = (dLon * 180.0) / (6378245.0 / sqrtMagic * Math.cos(radLat) * PI);
//            double mgLat = lat + dLat;
//            double mgLon = lon + dLon;
//            return new double[]{mgLat, mgLon};
//        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - 0.00669342162296595 * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((6335552.727 / (magic * sqrtMagic)) * PI);
        dLon = (dLon * 180.0) / (6378245.0 / sqrtMagic * Math.cos(radLat) * PI);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new double[]{mgLat, mgLon};
    }

    private static boolean isOutOfChina(double lat, double lon) {
        return (lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271);
    }

    private static double transformLat(double x, double y) {
        double res = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        res += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        res += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        res += (160.0 * Math.sin(y / 12.0 * PI) + 320.0 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return res;
    }

    private static double transformLon(double x, double y) {
        double res = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        res += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        res += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        res += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return res;
    }

}
