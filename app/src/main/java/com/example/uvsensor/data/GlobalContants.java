package com.example.uvsensor.data;

public class GlobalContants {

    public static final int NOTBEGIN = 0;
    public static final int RECORDING = 1;
    public static final int PAUSE = 2;
    public static final String STARTPAUSE = "start_pause_recording";  // 给notification用
    public static final String STOP = "stop_recording";  // 给notification用
    public static final String RETURNHOME = "return_home";
    public static final String TAG = "MainActivity";
    
    // 数据库表名
//    public static final String LOCATION_TABLE = "location";
    public static final String LOCATION_GPS_TABLE = "location_gps";
    public static final String LOCATION_NETWORK_TABLE = "location_network";
    public static final String ACCELEROMETER_TABLE = "accelerometer";
    public static final String ACCELEROMETER_UNCALIBRATED_TABLE = "accelerometer_uncalibrated";
    public static final String GYROSCOPE_TABLE = "gyroscope";
    public static final String GYROSCOPE_UNCALIBRATED_TABLE = "gyroscope_uncalibrated";
    public static final String GRAVITY_TABLE = "gravity";
    public static final String LINEAR_ACCELERATION_TABLE = "linear_acceleration";
    public static final String ROTATION_VECTOR_TABLE = "rotation_vector";
    public static final String MAGNETIC_FIELD_TABLE = "magnetic_field";
    //    private static final String ORIENTATION_TABLE = "orientation";
    public static final String LIGHT_TABLE = "light";
    public static final String PRESSURE_TABLE = "pressure";
    public static final String PROXIMITY_TABLE = "proximity";
    public static final String AMBIENT_TEMPERATURE_TABLE = "ambient_temperature";
    public static final String RELATIVE_HUMIDITY_TABLE = "relative_humidity";
    //    private static final String TEMPERATURE_TABLE = "temperature";
    public static final String STEP_COUNTER_TABLE = "step_counter";


    public static final String ACCELEROMETER_ENABLE = "accelerometer_enable";
    public static final String ACCELEROMETER_UNCALIBRATED_ENABLE = "accelerometerUncalibrated_enable";
    public static final String GYROSCOPE_ENABLE = "gyroscope_enable";
    public static final String GYROSCOPE_UNCALIBRATED_ENABLE = "gyroscopeUncalibrated_enable";
    public static final String GRAVITY_ENABLE = "gravity_enable";
    public static final String LINEAR_ACCELERATION_ENABLE = "linearAcceleration_enable";
    public static final String ROTATION_VECTOR_ENABLE = "rotationVector_enable";
    public static final String MAGNETIC_FIELD_ENABLE = "magneticField_enable";
    public static final String ORIENTATION_ENABLE = "orientation_enable";
    public static final String LIGHT_ENABLE = "light_enable";
    public static final String PRESSURE_ENABLE = "pressure_enable";
    public static final String PROXIMITY_ENABLE = "proximity_enable";
    public static final String AMBIENT_TEMPERATURE_ENABLE = "ambientTemperature_enable";
    public static final String RELATIVE_HUMIDITY_ENABLE = "relativeHumidity_enable";
    public static final String TEMPERATURE_ENABLE = "temperature_enable";
    public static final String STEP_COUNTER_ENABLE = "step_counter_enable";

}
