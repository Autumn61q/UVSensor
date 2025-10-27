# 🌞UVSensor
## 📖 Introduction
**UVSensor** is an Android application developed for the smartphone-based data collection. It leverages the device's built-in sensors and location services to provide the users with the data recording, visualizing and exporting functions.
## ✨ Main functions
### 1. Service configuration
**Calls the device’s location services and sensors to record environmental data based on the configuration.**  

<p align="center">
  <img src="https://github.com/user-attachments/assets/e97c049e-e8bc-47a3-8992-e2ac4eb70f24" alt="UVSensor screenshot" width="250">
</p>
<p align="center"><em>UVSensor configuration page</em></p>
UVSensor provides users with several commonly used location services and sensors: 
<div align="center">
  
Service Type | Service Name | Links for More Info
---|---|---
location | GPS location (GPS 定位) | [GPS_PROVIDER](https://developer.android.com/reference/android/location/LocationManager#GPS_PROVIDER)
location | Network location (网络定位) | [NETWORK_PROVIDER](https://developer.android.com/reference/android/location/LocationManager#NETWORK_PROVIDER)
sensor | Accelerometer (加速度计) | [TYPE_ACCELEROMETER](https://developer.android.com/reference/android/hardware/Sensor#TYPE_ACCELEROMETER)
sensor | Gyroscope (陀螺仪) | [TYPE_GYROSCOPE](https://developer.android.com/reference/android/hardware/Sensor#TYPE_GYROSCOPE)
sensor | Gravity sensor (重力传感器) | [TYPE_GRAVITY](https://developer.android.com/reference/android/hardware/Sensor#TYPE_GRAVITY)
sensor | Linear acceleration sensor (线性加速度传感器) | [TYPE_LINEAR_ACCELERATION](https://developer.android.com/reference/android/hardware/Sensor#TYPE_LINEAR_ACCELERATION)
sensor | Rotation vector sensor (旋转矢量传感器) | [TYPE_ROTATION_VECTOR](https://developer.android.com/reference/android/hardware/Sensor#TYPE_ROTATION_VECTOR)
sensor | Magnetic field sensor (磁场传感器) | [TYPE_MAGNETIC_FIELD](https://developer.android.com/reference/android/hardware/Sensor#TYPE_MAGNETIC_FIELD)
sensor | Light sensor (光线传感器) | [TYPE_LIGHT](https://developer.android.com/reference/android/hardware/Sensor#TYPE_LIGHT)
sensor | Pressure sensor (气压传感器) | [TYPE_PRESSURE](https://developer.android.com/reference/android/hardware/Sensor#TYPE_PRESSURE)
sensor | Proximity sensor (近程传感器) | [TYPE_PROXIMITY](https://developer.android.com/reference/android/hardware/Sensor#TYPE_PROXIMITY)
sensor | Ambient temperature sensor (环境温度传感器) | [TYPE_AMBIENT_TEMPERATURE](https://developer.android.com/reference/android/hardware/Sensor#TYPE_AMBIENT_TEMPERATURE)
sensor | Relative humidity sensor (相对湿度传感器) | [TYPE_RELATIVE_HUMIDITY](https://developer.android.com/reference/android/hardware/Sensor#TYPE_RELATIVE_HUMIDITY)
sensor | Step counter (计步器) | [TYPE_STEP_COUNTER](https://developer.android.com/reference/android/hardware/Sensor#TYPE_STEP_COUNTER)
</div>

> **Tips**
> 1. Opening at least one of the **location types** is mandatory for subsequent data recording.  
> 2. UVSensor automatically disables unsupported service types on the current device, and those switches cannot be opened.
> 3. Each service block includes a collapsible section — click it to navigate to the corresponding detail page.

<p align="center">
  <img src="https://github.com/user-attachments/assets/144261c2-a0b8-4111-ac71-8bc6f37954b7" alt="config & detail" width="730">
</p> 
<p align="center"><em>Navigate to the corresponding detail page</em></p>

### 2. Diverse visualizations
**Visualize the the data during recording**  
The trajectory, gotten from the location points, are drawn on the map during the recording.  

<p align="center">
  <a href="https://github.com/user-attachments/assets/0f5511d1-89ec-44d2-a6c1-b7068d0b129a">
    <img src="https://github.com/user-attachments/assets/24eba1fb-b356-460d-a876-cb527596c649" alt="traj_demo" width="250">
  </a>
</p>
<p align="center"><em>Trajectory demo</em></p>



> **Tips**
> 1.Trajectory is updated in real-time, while data on the coordinate axes is updated every two seconds.

### 3. Data Export for Analysis
## 🔎 Tips with more details
