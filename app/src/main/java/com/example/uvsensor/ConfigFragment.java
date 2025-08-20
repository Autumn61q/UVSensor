package com.example.uvsensor;

import static com.example.uvsensor.data.GlobalContants.*;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.uvsensor.adapter.ConfigAdapter;
import com.example.uvsensor.bean.ConfigItem;
import com.example.uvsensor.bean.DividerItem;
import com.example.uvsensor.bean.LocationType;
import com.example.uvsensor.bean.SensorType;
import com.example.uvsensor.listener.ConfigChangedListener;
import com.example.uvsensor.listener.ToDetailFragmentListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfigFragment extends Fragment {
    private ConfigAdapter configAdapter;
    private List<ConfigItem> TypeList;
    private RecyclerView recyclerView;
    private SensorManager sensorManager;
    private List<String> locationConfigList;
    private List<Integer> sensorConfigList;  // 反正就是得写integer不能写int
//    private ConfigChangedListener configChangedListener;


    public ConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_config, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initComponent(root);
        }

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initComponent(View root) {

        sensorConfigList = new ArrayList<>();
        locationConfigList = new ArrayList<>();

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);

        TypeList = new ArrayList<>();

        TypeList.add((new LocationType("GPS定位", LocationManager.GPS_PROVIDER, LOCATION_GPS_TABLE)));
        TypeList.add((new LocationType("网络定位", LocationManager.NETWORK_PROVIDER, LOCATION_NETWORK_TABLE)));

        TypeList.add(new DividerItem());

        TypeList.add(new SensorType("加速度计", Sensor.TYPE_ACCELEROMETER, ACCELEROMETER_TABLE, hasSensor(Sensor.TYPE_ACCELEROMETER)));
        TypeList.add(new SensorType("陀螺仪", Sensor.TYPE_GYROSCOPE, GYROSCOPE_TABLE, hasSensor(Sensor.TYPE_GYROSCOPE)));
        TypeList.add(new SensorType("重力传感器", Sensor.TYPE_GRAVITY, GRAVITY_TABLE, hasSensor(Sensor.TYPE_GRAVITY)));
        TypeList.add(new SensorType("线性加速度传感器", Sensor.TYPE_LINEAR_ACCELERATION, LINEAR_ACCELERATION_TABLE, hasSensor(Sensor.TYPE_LINEAR_ACCELERATION)));
        TypeList.add(new SensorType("旋转矢量传感器", Sensor.TYPE_ROTATION_VECTOR, ROTATION_VECTOR_TABLE, hasSensor(Sensor.TYPE_ROTATION_VECTOR)));
        TypeList.add(new SensorType("磁场传感器", Sensor.TYPE_MAGNETIC_FIELD, MAGNETIC_FIELD_TABLE, hasSensor(Sensor.TYPE_MAGNETIC_FIELD)));
        TypeList.add(new SensorType("光线传感器", Sensor.TYPE_LIGHT, LIGHT_TABLE, hasSensor(Sensor.TYPE_LIGHT)));
        TypeList.add(new SensorType("气压传感器", Sensor.TYPE_PRESSURE, PRESSURE_TABLE, hasSensor(Sensor.TYPE_PRESSURE)));
        TypeList.add(new SensorType("近程传感器", Sensor.TYPE_PROXIMITY, PROXIMITY_TABLE, hasSensor(Sensor.TYPE_PROXIMITY)));
        TypeList.add(new SensorType("环境温度传感器", Sensor.TYPE_AMBIENT_TEMPERATURE, AMBIENT_TEMPERATURE_TABLE, hasSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)));
        TypeList.add(new SensorType("相对湿度传感器", Sensor.TYPE_RELATIVE_HUMIDITY, RELATIVE_HUMIDITY_TABLE, hasSensor(Sensor.TYPE_RELATIVE_HUMIDITY)));
        TypeList.add(new SensorType("计步器", Sensor.TYPE_STEP_COUNTER, STEP_COUNTER_TABLE, hasSensor(Sensor.TYPE_STEP_COUNTER)));


        configAdapter = new ConfigAdapter(TypeList, requireContext());

        recyclerView = root.findViewById(R.id.rcv_itemType_list);

        recyclerView.setAdapter(configAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        configAdapter.setConfigChangedListener(new ConfigChangedListener() {
            @Override
            public void deleteSensor(int sensor_type) {
                sensorConfigList.remove(Integer.valueOf(sensor_type));
            }

            @Override
            public void addSensor(int sensor_type) {
                sensorConfigList.add(sensor_type);
            }

            @Override
            public void deleteLocationType(String location_type) {
                locationConfigList.remove(location_type);
            }

            @Override
            public void addLocationType(String location_type) {
                locationConfigList.add(location_type);
            }
        });

        configAdapter.setToDetailFragmentListener(new ToDetailFragmentListener() {
            @Override
            public void onDetailClick(ConfigItem itemType) {
                Bundle args = new Bundle();
                args.putSerializable("item_type", itemType);

                ((MainActivity) requireActivity()).setDetailFragmentBundle(args);
            }
        });
    }

    // 检查一下设备里面有没有对应的传感器，如果有的话我们就默认打开（通过别的文件中的代码逻辑实现），并添加到configList里面
    private boolean hasSensor(int sensor_type) {
        if (sensorManager.getDefaultSensor(sensor_type) == null) {
            return false;
        } else {
            sensorConfigList.add(sensor_type);
            return true;
        }
    }

    public List<Integer> getSensorConfigList() {
        return sensorConfigList;
    }

    public List<String> getLocationConfigList() {
        return locationConfigList;
    }

}
