package com.example.uvsensor.listener;

import android.location.Address;
import android.location.Location;

public interface AddressCallback {  // 哥们虽然名字不带listener，但哥们干着listener的活
    void onGetAddress(Address address);
    void onGetLocation(Location location);
    void onGetFirstLocation(Location location);
}
