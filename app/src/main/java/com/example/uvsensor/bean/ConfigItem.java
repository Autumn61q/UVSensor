package com.example.uvsensor.bean;

import java.io.Serializable;

public abstract class ConfigItem implements Serializable {  // 就是它的子类现在有SensorType和LocationType
    private String item_name;
    private String item_table_name;  // 数据库对应的名字，从global constant里面找table结尾的变量名
    public abstract int getType();

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }


    public String getItem_table_name() {
        return item_table_name;
    }

    public void setItem_table_name(String item_table_name) {
        this.item_table_name = item_table_name;
    }
}
