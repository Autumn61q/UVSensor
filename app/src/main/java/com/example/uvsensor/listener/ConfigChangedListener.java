package com.example.uvsensor.listener;

// 如果用户有更改传感器配置，我们就通过这个接口在adapter和configFragment之间传递信息
// 以及，直接在configFragment设置配置的传感器名单（到时候传给serivice）。1. 不用建两个list，节约内存
//
public interface ConfigChangedListener {
    public void deleteSensor(int sensor_type);
    public void addSensor(int sensor_type);
    public void deleteLocationType(String location_type);
    public void addLocationType(String location_type);
}