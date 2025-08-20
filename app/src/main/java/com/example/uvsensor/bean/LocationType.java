package com.example.uvsensor.bean;

import java.io.Serializable;

public class LocationType extends ConfigItem implements Serializable {

//    private String location_name;
    private String location_type;
    private boolean checked;
    public LocationType(String item_name, String location_type, String table_name) {
        super();
        this.setItem_name(item_name);
        this.location_type = location_type;
        this.setItem_table_name(table_name);
    }


//    public String getLocation_name() {
//        return location_name;
//    }
//
//    public void setLocation_name(String location_name) {
//        this.location_name = location_name;
//    }

    public String getLocation_type() {
        return location_type;
    }

    public void setLocation_type(String location_type) {
        this.location_type = location_type;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public int getType() {
        return 1;
    }
}
