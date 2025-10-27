# 🌞UVSensor
## 📖 Introduction
**UVSensor** is an Android application developed for the smartphone-based data collection. It leverages the device's built-in sensors and location services to provide the users with the data recording, visualizing and exporting functions.
## 📍 Quick Start
1. Clone this repository:
```
git clone https://github.com/Autumn61q/UVSensor.git
cd UVSensor
```
2. Run in Android Studio  
3. Connect your Android device (or start an emulator)  
4. Start!
## 🗺️ Main functions
### 1. Service configuration
**Call the device’s location services and sensors to record environmental data based on the configuration.**  

<p align="center">
  <img src="https://github.com/user-attachments/assets/e97c049e-e8bc-47a3-8992-e2ac4eb70f24" alt="UVSensor screenshot" width="250">
</p>
<p align="center"><em>Figure: UVSensor configuration page</em></p>
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

Each service block includes a collapsible section — click it to navigate to the corresponding detail page.

<p align="center">
  <img src="https://github.com/user-attachments/assets/144261c2-a0b8-4111-ac71-8bc6f37954b7" alt="config & detail" width="730">
</p> 
<p align="center"><em>Figure: Navigate to the corresponding detail page</em></p>

> **Tips**
> 1. Opening at least one of the **location types** is mandatory for subsequent data recording.  
> 2. UVSensor automatically disables unsupported service types on the current device, and those switches cannot be opened.

### 2. Diverse visualizations
**Visualize the the data during recording**  
### 2.1 Trajectory visualization
The trajectory, gotten from the location points, are drawn on the map during the recording.  

<p align="center">
  <a href="https://github.com/user-attachments/assets/0f5511d1-89ec-44d2-a6c1-b7068d0b129a">
    <img src="https://github.com/user-attachments/assets/24eba1fb-b356-460d-a876-cb527596c649" alt="traj_demo" width="250">
  </a>
</p>
<p align="center"><em>Video: Trajectory visualization demo</em></p>

Users can select which trajectories to show through <img src="https://github.com/user-attachments/assets/bf4b18b5-b403-4f8f-9d57-28ecc58f8c95" alt="icon" width="20">, GPS or Network, if the corresponding location service is active during the recording.  
GPS trajectory is blue color scheme, while Network trajectory is green color scheme. Different <a href="https://developer.android.com/reference/android/location/Location.html#getAccuracy()" target="_blank">location accuracy</a>  corresponds to different color intensities. The table below gives more specific correspondence:
<div align="center">
  
| Location type | Accuracy range | Color (Hex) | Preview |
|---------------|----------------|-------------|---------|
| **GPS**       | ≤ 3 m          | `#0ebeff`   | <img src="https://github.com/user-attachments/assets/8e1ccd62-1cc1-426c-af1e-d768a2a14403" alt="icon" style="width:15px; height:15px; object-fit:cover;"> |
| GPS           | ≤ 7 m          | `#44ccff`   | <img src="https://github.com/user-attachments/assets/368355f1-62b6-4393-8f8b-0d21a7648c01" alt="icon" style="width:15px; height:15px; object-fit:cover;"> |
| GPS           | ≤ 30 m         | `#79dbff`   | <img src="https://github.com/user-attachments/assets/b47c971b-af88-4d01-a8b2-d28b58458004" alt="icon" style="width:15px; height:15px; object-fit:cover;"> |
| GPS           | > 30 m         | `#afe9ff`   | <img src="https://github.com/user-attachments/assets/d72c4dbc-c503-45c5-9ecd-69a4e09f6f3e" alt="icon" style="width:15px; height:15px; object-fit:cover;"> |
| **Network**   | ≤ 3 m          | `#26ba58`   | <img src="https://github.com/user-attachments/assets/009b764f-3f10-4685-bdef-76a7a3c8ff6e" alt="icon" style="width:15px; height:15px; object-fit:cover;"> |
| Network       | ≤ 7 m          | `#56c97d`   | <img src="https://github.com/user-attachments/assets/8aa5b488-4cd3-4f10-bbad-400f7242eb3e" alt="icon" style="width:15px; height:15px; object-fit:cover;"> |
| Network       | ≤ 30 m         | `#9fe0b5`   | <img src="https://github.com/user-attachments/assets/604d03ef-4d0a-4929-95ac-26d488eda70b" alt="icon" style="width:15px; height:15px; object-fit:cover;"> |
| Network       | > 30 m         | `#ccffda`   | <img src="https://github.com/user-attachments/assets/1b21f408-714d-48bd-bda5-32573a3ee9b2" alt="icon" style="width:15px; height:15px; object-fit:cover;"> |
</div>

### 2.2 Coordinate axes visualization
The location accuracy, as well as the data recorded by the sensors, are visualized on axes.

<p align="center">
  <a href="https://github.com/user-attachments/assets/1a808f4d-6582-472f-beec-5ea4cd5bf178">
    <img src="https://github.com/user-attachments/assets/c0579e88-ea00-4673-95df-297ee889393f" alt="traj_demo" width="250">
  </a>
</p>
<p align="center"><em>Video: Coordinate axes visualization demo: accelerometer</em></p>

> **Tips**  
> 1. Trajectory is updated in real-time, while data on the coordinate axes is updated every two seconds.

### 3. Data Export for Analysis
When the recording finishes, users can export the recording data for further analysis.  
All data will be exported as CSV files. A KML file is generated additionally for location points, so that it can be visualized in <a href="https://www.google.com/earth/">Google Earth</a>.

<p align="center">
  <img src="https://github.com/user-attachments/assets/e4ff7406-7002-495a-a88f-d4494754cd32" alt="config & detail" width="730">
</p> 
<p align="center"><em>Figure: The exported data</em></p>
