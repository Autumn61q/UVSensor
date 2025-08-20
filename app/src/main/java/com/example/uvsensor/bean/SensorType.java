package com.example.uvsensor.bean;

import java.io.Serializable;

public class SensorType extends ConfigItem implements Serializable {
//    private String sensor_name;
    private int sensor_type; // 就是Sensor.TYPE_ACCELEROMETER这种
//    private String table_name;  // 数据库对应的名字，从global constant里面找table结尾的变量名
    private boolean exist;  // 布尔值不初始化的话，默认的值是false
    private boolean checked;  // 这个是为了让viewHolder知道这个传感器对应的开关有没有被打开。。。服了

    public SensorType(String sensor_name, int sensor_type, String table_name, boolean exist) {
        this.setItem_name(sensor_name);
        this.sensor_type = sensor_type;
        this.setItem_table_name(table_name);
        this.exist = exist;
        this.checked = true;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

//    public String getSensor_name() {
//        return this.getItem_name();
//    }

    public int getSensor_type() {
        return sensor_type;
    }

    // 我们应该直接调用父类的get & set方法
    public void setSensor_type(int sensor_type) {
        this.sensor_type = sensor_type;
    }

//    public String getTable_name() {
//        return table_name;
//    }
//
//    public void setTable_name(String table_name) {
//        this.table_name = table_name;
//    }

//    public void setSensor_name(String sensor_name) {
//        this.setItem_name(sensor_name);
//    }

    @Override
    public int getType() {
        return 0;
    }
}
