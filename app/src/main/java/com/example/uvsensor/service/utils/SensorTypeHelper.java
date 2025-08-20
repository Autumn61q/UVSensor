package com.example.uvsensor.service.utils;

import static android.hardware.Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
import static com.example.uvsensor.data.GlobalContants.*;

import android.hardware.Sensor;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class SensorTypeHelper {
    public static int getServiceType(String s) {
        switch (s) {
            // 运动传感器
            case ACCELEROMETER_ENABLE:
                return Sensor.TYPE_ACCELEROMETER;
            case ACCELEROMETER_UNCALIBRATED_ENABLE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return Sensor.TYPE_ACCELEROMETER_UNCALIBRATED;
                }
            case GYROSCOPE_ENABLE:
                return Sensor.TYPE_GYROSCOPE;
            case GYROSCOPE_UNCALIBRATED_ENABLE:
                return TYPE_GYROSCOPE_UNCALIBRATED;
            case GRAVITY_ENABLE:
                return Sensor.TYPE_GRAVITY;
            case LINEAR_ACCELERATION_ENABLE:
                return Sensor.TYPE_LINEAR_ACCELERATION;
            case ROTATION_VECTOR_ENABLE:
                return Sensor.TYPE_ROTATION_VECTOR;
                
            // 位置传感器
            case MAGNETIC_FIELD_ENABLE:
                return Sensor.TYPE_MAGNETIC_FIELD;
            case ORIENTATION_ENABLE:
                return Sensor.TYPE_ORIENTATION;
                
            // 环境传感器
            case LIGHT_ENABLE:
                return Sensor.TYPE_LIGHT;
            case PRESSURE_ENABLE:
                return Sensor.TYPE_PRESSURE;
            case PROXIMITY_ENABLE:
                return Sensor.TYPE_PROXIMITY;
            case AMBIENT_TEMPERATURE_ENABLE:
                return Sensor.TYPE_AMBIENT_TEMPERATURE;
            case RELATIVE_HUMIDITY_ENABLE:
                return Sensor.TYPE_RELATIVE_HUMIDITY;
            case TEMPERATURE_ENABLE:
                return Sensor.TYPE_TEMPERATURE;
                
            default:
                return 0;
        }
    }
    
    public static String getSensorName(int sensorType) {
        switch (sensorType) {
            // 运动传感器
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                return "Accelerometer Uncalibrated";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case TYPE_GYROSCOPE_UNCALIBRATED:
                return "Gyroscope Uncalibrated";
            case Sensor.TYPE_GRAVITY:
                return "Gravity";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Linear Acceleration";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Rotation Vector";
                
            // 位置传感器
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetometer";
            case Sensor.TYPE_ORIENTATION:
                return "Orientation (Deprecated)";
                
            // 环境传感器
            case Sensor.TYPE_LIGHT:
                return "Light";
            case Sensor.TYPE_PRESSURE:
                return "Pressure";
            case Sensor.TYPE_PROXIMITY:
                return "Proximity";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "Ambient Temperature";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Relative Humidity";
            case Sensor.TYPE_TEMPERATURE:
                return "Temperature (Deprecated)";
                
            default:
                return "Unknown Sensor";
        }
    }

    public static List<String> getAll() {
        List<String> allConfigChoices = new ArrayList<>();

        allConfigChoices.add(ACCELEROMETER_ENABLE);
        allConfigChoices.add(ACCELEROMETER_UNCALIBRATED_ENABLE);
        allConfigChoices.add(GYROSCOPE_ENABLE);
        allConfigChoices.add(GYROSCOPE_UNCALIBRATED_ENABLE);
        allConfigChoices.add(GRAVITY_ENABLE);
        allConfigChoices.add(LINEAR_ACCELERATION_ENABLE);
        allConfigChoices.add(ROTATION_VECTOR_ENABLE);
        allConfigChoices.add(MAGNETIC_FIELD_ENABLE);
//        allConfigChoices.add(ORIENTATION_ENABLE);
        allConfigChoices.add(LIGHT_ENABLE);
        allConfigChoices.add(PRESSURE_ENABLE);
        allConfigChoices.add(PROXIMITY_ENABLE);
        allConfigChoices.add(AMBIENT_TEMPERATURE_ENABLE);
        allConfigChoices.add(RELATIVE_HUMIDITY_ENABLE);
//        allConfigChoices.add(TEMPERATURE_ENABLE);
        return allConfigChoices;
    }
}
