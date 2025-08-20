package com.example.uvsensor.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.example.uvsensor.service.utils.RecordingDatabaseHelper;
import com.example.uvsensor.service.utils.SensorTypeHelper;

public class MySensorEventListener implements SensorEventListener {

    private RecordingDatabaseHelper recordingDatabaseHelper;
    private static final String TAG = "MySensorEventListener";

    // 不建议构造多个databasehelper
//    public MySensorEventListener(Context context, String createdTime) {
//        this.recordingDatabaseHelper = new RecordingDatabaseHelper(context, createdTime);
//    }

    public MySensorEventListener(RecordingDatabaseHelper recordingDatabaseHelper) {
        this.recordingDatabaseHelper = recordingDatabaseHelper;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        String sensorName = SensorTypeHelper.getSensorName(sensorType);

        // Log sensor data for debugging
        if (sensorEvent.values.length >= 3) {
            Log.d(TAG, sensorName + " data[x:" + sensorEvent.values[0] +
                    ", y:" + sensorEvent.values[1] + ", z:" + sensorEvent.values[2] + "]");
        } else {
            Log.d(TAG, sensorName + " data: " + java.util.Arrays.toString(sensorEvent.values));
        }

        // Handle specific sensor types
        switch (sensorType) {
            // 运动传感器
            case Sensor.TYPE_ACCELEROMETER:
                recordingDatabaseHelper.insertAccelerometerData(sensorEvent);
                break;
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                recordingDatabaseHelper.insertUncalibratedAccelerometerData(sensorEvent);
                break;
            case Sensor.TYPE_GYROSCOPE:
                recordingDatabaseHelper.insertGyroscopeData(sensorEvent);
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                recordingDatabaseHelper.insertUncalibratedGyroscopeData(sensorEvent);
                break;
            case Sensor.TYPE_GRAVITY:
                recordingDatabaseHelper.insertGravityData(sensorEvent);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                recordingDatabaseHelper.insertLinearAccelerationData(sensorEvent);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                recordingDatabaseHelper.insertRotationVectorData(sensorEvent);
                break;
            case Sensor.TYPE_STEP_COUNTER:
                recordingDatabaseHelper.insertStepCounterData(sensorEvent);
                break;

            // 位置传感器
            case Sensor.TYPE_MAGNETIC_FIELD:
                recordingDatabaseHelper.insertMagneticFieldData(sensorEvent);
                break;
//            case Sensor.TYPE_ORIENTATION:
//                recordingDatabaseHelper.insertOrientationData(sensorEvent);
//                break;

            // 环境传感器
            case Sensor.TYPE_LIGHT:
                recordingDatabaseHelper.insertLightData(sensorEvent);
                break;
            case Sensor.TYPE_PRESSURE:
                recordingDatabaseHelper.insertPressureData(sensorEvent);
                break;
            case Sensor.TYPE_PROXIMITY:
                recordingDatabaseHelper.insertProximityData(sensorEvent);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                recordingDatabaseHelper.insertAmbientTemperatureData(sensorEvent);
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                recordingDatabaseHelper.insertRelativeHumidityData(sensorEvent);
                break;
//            case Sensor.TYPE_TEMPERATURE:
//                recordingDatabaseHelper.insertTemperatureData(sensorEvent);
//                break;

            default:
                Log.d(TAG, "Unhandled sensor type: " + sensorName);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    // 🔧 Add cleanup method for database batching
    public void cleanup() {
        if (recordingDatabaseHelper != null) {
            recordingDatabaseHelper.forceCommitBatch();
            Log.d(TAG, "Database batch committed on cleanup");
        }
    }
}
