# 🌞UVSensor
## 📖 Introduction
**UVSensor** is an Android application developed for the smartphone-based data collection. It leverages the device's built-in sensors and location services to provide the users with the data recording, visualizing and exporting functions.
## ✨ Main functions
### 1. Smart Sensor Integration 
**Calls the device’s location services and sensors to record environmental data based on the configuration.**
<img src="https://github.com/user-attachments/assets/e97c049e-e8bc-47a3-8992-e2ac4eb70f24" alt="UVSensor screenshot" width="480">
UVSensor provides users with several commonly used location services and sensors:
service type | service name | links for more info
---|---|---
location | GPS location | https://developer.android.com/reference/android/location/LocationManager#GPS_PROVIDER 
location | Network location | https://developer.android.com/reference/android/location/LocationManager#NETWORK_PROVIDER
motion | Accelerometer (加速度计) | [Sensor.TYPE_ACCELEROMETER](https://developer.android.com/reference/android/hardware/Sensor#TYPE_ACCELEROMETER)
motion | Gyroscope (陀螺仪) | [Sensor.TYPE_GYROSCOPE](https://developer.android.com/reference/android/hardware/Sensor#TYPE_GYROSCOPE)
motion | Gravity sensor (重力传感器) | [Sensor.TYPE_GRAVITY](https://developer.android.com/reference/android/hardware/Sensor#TYPE_GRAVITY)
motion | Linear acceleration sensor (线性加速度传感器) | [Sensor.TYPE_LINEAR_ACCELERATION](https://developer.android.com/reference/android/hardware/Sensor#TYPE_LINEAR_ACCELERATION)
motion | Rotation vector sensor (旋转矢量传感器) | [Sensor.TYPE_ROTATION_VECTOR](https://developer.android.com/reference/android/hardware/Sensor#TYPE_ROTATION_VECTOR)
position | Magnetic field sensor (磁场传感器) | [Sensor.TYPE_MAGNETIC_FIELD](https://developer.android.com/reference/android/hardware/Sensor#TYPE_MAGNETIC_FIELD)
environment | Light sensor (光线传感器) | [Sensor.TYPE_LIGHT](https://developer.android.com/reference/android/hardware/Sensor#TYPE_LIGHT)
environment | Pressure sensor (气压传感器) | [Sensor.TYPE_PRESSURE](https://developer.android.com/reference/android/hardware/Sensor#TYPE_PRESSURE)
position | Proximity sensor (近程传感器) | [Sensor.TYPE_PROXIMITY](https://developer.android.com/reference/android/hardware/Sensor#TYPE_PROXIMITY)
environment | Ambient temperature sensor (环境温度传感器) | [Sensor.TYPE_AMBIENT_TEMPERATURE](https://developer.android.com/reference/android/hardware/Sensor#TYPE_AMBIENT_TEMPERATURE)
environment | Relative humidity sensor (相对湿度传感器) | [Sensor.TYPE_RELATIVE_HUMIDITY](https://developer.android.com/reference/android/hardware/Sensor#TYPE_RELATIVE_HUMIDITY)
motion | Step counter (计步器) | [Sensor.TYPE_STEP_COUNTER](https://developer.android.com/reference/android/hardware/Sensor#TYPE_STEP_COUNTER)

2. Diverse visualizations for the recording data
3. Enable the data exportation for further analysis
