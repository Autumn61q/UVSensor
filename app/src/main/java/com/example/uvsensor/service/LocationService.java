package com.example.uvsensor.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.uvsensor.R;
import com.example.uvsensor.data.GlobalContants;
import com.example.uvsensor.listener.AddressCallback;
import com.example.uvsensor.listener.MySensorEventListener;
import com.example.uvsensor.listener.RecordingStateListener;
import com.example.uvsensor.service.utils.RecordingDatabaseHelper;
import com.example.uvsensor.service.utils.SensorTypeHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service {

    private List<Integer> configList;
    private List<String> locationProviders;
    private int sample_period;
    private static final String CHANNEL_ID = "recording_location";
    public static final int FOREGROUND_ID = 1;
    private LocationManager locationManager;
    private Context mContext;
    private static ArrayList<AddressCallback> addressCallbacks;  // 创建一个 addresscallback 列表是为了防止很多个activity/fragment都创建addresscallback实例的情况。比方说一个fragment用它获得天气，一个fragment用它绘制轨迹之类
    private AddressCallback addressCallback;
    private static Location location;
    private boolean isInit = false;  // 是否被加载过
    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews remoteView;
    private boolean isRecording = true;
    private RecordingStateListener recordingStateListener;
    private SensorManager sensorManager;
    private MySensorEventListener sensorEventListener;
    private RecordingDatabaseHelper recordingDatabaseHelper;
    private String createdTime;

    private BroadcastReceiver notificaitonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case GlobalContants.STARTPAUSE:
                    if (isRecording) {
                        pauseRecording();
                        Log.d("LocationService", "pause is clicked");
                    } else {
                        continueRecording();
                    }
                    break;
                case GlobalContants.STOP:
                    recordingStateListener.onStopRecording();
                    Log.d("LocationService", "stop is clicked");
                    break;
                case GlobalContants.RETURNHOME:
//                    returnHome();
                    break;
            }
        }
    };

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location newLocation) {
            location = newLocation; // Update global location with fresh data
            recordingDatabaseHelper.insertLocationData(location);
            Log.d("LocationService", String.valueOf(location.getLatitude()) + location.getLongitude());
            showLocation(); // Notify callbacks with fresh location
        }

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(@NonNull List<Location> locations) {
            LocationListener.super.onLocationChanged(locations);
        }

        // LocationProvider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            LocationListener.super.onStatusChanged(provider, status, extras);
        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(@NonNull String provider) {
            LocationListener.super.onProviderEnabled(provider);
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(@NonNull String provider) {
            LocationListener.super.onProviderDisabled(provider);
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this; // Service IS a Context
        addressCallbacks = new ArrayList<>();
        sample_period = 500;
        configList = new ArrayList<>();
        locationProviders = new ArrayList<>();
        createdTime = String.valueOf(System.currentTimeMillis());
        recordingDatabaseHelper = new RecordingDatabaseHelper(this, createdTime);

//        Log.d("LocationService", "onCreate: createdTime=" + createdTime);
//        Log.d("LocationService", "onCreate: db_name=" + recordingDatabaseHelper.getDatabaseName());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorEventListener = new MySensorEventListener(recordingDatabaseHelper);

//        getLocation(); // Initialize location tracking 去onStartCommand里面调用，后者比onCreate完调用。若在此时调用，locationProviders为空

//        Log.d("LocationService", "Service created");

        IntentFilter notificationFilter = new IntentFilter();
        notificationFilter.addAction(GlobalContants.STARTPAUSE);
        notificationFilter.addAction(GlobalContants.STOP);
        notificationFilter.addAction(GlobalContants.RETURNHOME);
        
        // Use RECEIVER_NOT_EXPORTED for Android 12+ compatibility
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificaitonReceiver, notificationFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(notificaitonReceiver, notificationFilter);
        }

        createNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            ArrayList<Integer> newConfigList = intent.getIntegerArrayListExtra("configList");
            ArrayList<String> newLocationProviders = intent.getStringArrayListExtra("locationProvider");

            if (newConfigList != null ) {
                configList = newConfigList;
            }
            if (newLocationProviders != null) {
                locationProviders = newLocationProviders;
            }

        } else {
            // Use previous configuration if available
            if (configList == null) {
                configList = new ArrayList<>();
                Log.w("LocationService", "No previous configuration available");
            }
            if (locationProviders == null) {
                locationProviders = new ArrayList<>();
                Log.w("LocationService", "No previous location Providers available");
            }
        }

        getLocation(); // Initialize location tracking

        startSensorRecording();

        if (notification == null) {
            createNotification();
        }

        return START_STICKY; // Restart service if killed by system

    }

    private void startSensorRecording() {
        // 🔍 Debug logging to diagnose the issue
//        Log.d("LocationService", "=== startSensorRecording() DEBUG ===");
//        Log.d("LocationService", "configList is null: " + (configList == null));
        if (configList != null) {
//            Log.d("LocationService", "configList size: " + configList.size());
//            Log.d("LocationService", "configList contents: " + configList.toString());
        }
        if (locationProviders != null) {
//            Log.d("LocationService", "locationProviders size: " + locationProviders.size());
//            Log.d("LocationService", "locationProviders contents: " + locationProviders.toString());
        }

        for (Integer sensorType : configList) {
            if (sensorType != 0) {
                // 注册传感器
                Sensor sensor = sensorManager.getDefaultSensor(sensorType);
                if (sensor != null) {
                    boolean success = sensorManager.registerListener(
                            sensorEventListener,
                            sensor,
                            (int) (sample_period * 1e3)  // 单位是微秒。咱的smaple_period的单位是毫秒，所以还得乘1e3
                    );
                }
            }
        }
    }

    private void stopSensorRecording() {
        if (sensorManager != null && sensorEventListener != null) {
            sensorManager.unregisterListener(sensorEventListener);
            sensorEventListener.cleanup(); // 🔧 Commit any pending database batches
//            Log.d("LocationService", "All sensors unregistered and database cleaned up");
        }
    }

    // 创建notification
    private void createNotification() {
        notificationManager = getSystemService(NotificationManager.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "传感器记录通知",
                    NotificationManager.IMPORTANCE_LOW
            );
            
            channel.setDescription("channel for location service");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        int notificationLayout = R.layout.notification_recording_layout;
        remoteView = new RemoteViews(getPackageName(), notificationLayout);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        flags |= PendingIntent.FLAG_IMMUTABLE;

        Intent startPauseIntent = new Intent(GlobalContants.STARTPAUSE);
        startPauseIntent.setPackage(getPackageName());  // PackageName is com.example.uvsensor。 不加上这条命令的话，系统不知道把intent给哪个receiver。点击按钮就没反应
        PendingIntent startPausePendIntent = PendingIntent.getBroadcast(this, 0, startPauseIntent, flags);
        remoteView.setOnClickPendingIntent(R.id.iv_StartPause, startPausePendIntent);

        Intent stopIntent = new Intent(GlobalContants.STOP);
        stopIntent.setPackage(getPackageName());
        PendingIntent stopPendIntent = PendingIntent.getBroadcast(this, 1, stopIntent, flags);
        remoteView.setOnClickPendingIntent(R.id.iv_Stop, stopPendIntent);

//        Intent ReturnHomeIntent = new Intent(GlobalContants.RETURNHOME);
//        PendingIntent ReturnHomePendIntent = PendingIntent.getBroadcast(this, 0, ReturnHomeIntent, flags);
//        remoteView.setOnClickPendingIntent(R.id.rl_notification_container, ReturnHomePendIntent);
        
        
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentText("正在获取传感器信息")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCustomContentView(remoteView)
                .setSmallIcon(R.drawable.rui_logo)
                .build();

        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LocationService", "Service destroyed");

        try {
            unregisterReceiver(notificaitonReceiver);
        } catch (Exception e) {
            Log.e("LocationService", "Error unregistering receiver: " + e.getMessage());
        }

        removeLocationUpdatesListener();
        clearAddressCallback();
    }

    public AddressCallback getAddressCallback(){
        return addressCallback;
    }

    public void setAddressCallback(AddressCallback addressCallback){
        this.addressCallback = addressCallback;
        addAddressCallback(this.addressCallback);
        if (isInit) {
            showLocation();
        }
        else {
            isInit = true;
        }
    }

    private void addAddressCallback(AddressCallback addressCallback){
        addressCallbacks.add(addressCallback);
        if (isInit) {
            showLocation();
        }
    }

    // 我觉得我永远也想不到什么时候要调用这个方法
    private void removeAddressCallback(AddressCallback addressCallback){
        if (addressCallbacks.contains(addressCallback)){
            addressCallbacks.remove(addressCallback);
        }
    }

    public void clearAddressCallback(){
        removeLocationUpdatesListener();
        addressCallbacks.clear();
    }

    private void removeLocationUpdatesListener() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    // 将location.Latitude和location.Longitude传给监听器，在fragment的地图中绘制
    private void showLocation() {
        if (location == null) {
            getLocation();
        }
        else {
            if (addressCallback != null) {
                addressCallback.onGetLocation(location);
            }
        }
    }

    // 更新location.Latitude和location.Longitude
    private void getLocation() {
        // 获取位置管理器
        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;  // 如果用户没给权限，就返回。
        }

        List<String> providerList = locationManager.getProviders(true);
//        String locationProvider;

        for (String locationProvider : locationProviders) {
            if (providerList.contains(locationProvider)) {
                Log.d("LocationService", "gotta register" + locationProvider);
                // after requestLocationUpdates, LocationListener callbacks are triggered
                // new location 会自动被传到locationListener里面
                locationManager.requestLocationUpdates(locationProvider, sample_period, 0, locationListener);
            }

            // getLastKnownLocation returns the most recent cached location from the past currently available.
            // 缺点就是返回的是之前的位置（从别的软件获取的或者怎么着的），优点是快和省电
            // Will return null if no such cached location is available. 一般第一次调用时会返回null
            // 无所谓了，我们就让最后一个locationProvider提供位置吧，并且我们改变一下地图的中心点
            location = locationManager.getLastKnownLocation(locationProvider);
            if (addressCallback != null) {
                addressCallback.onGetFirstLocation(location);
            }
        }

        if (location != null) {
            Log.d("Location TAG", "显示设备以前位置信息");
            // 首先我们显示历史位置，然后立马我们在下面requestLocationUpdates
            showLocation();
        }
    }

//    private void getLngAndLatWithNetwork() {
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        // 获取位置管理器
//        if (locationManager == null) {
//            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
//        }
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 1, locationListener);
//        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        showLocation();
//    }

    private void getAddress(double latitude, double longitude) throws IOException {
        Geocoder gc = new Geocoder(mContext, Locale.getDefault());

        List<Address> locationList = gc.getFromLocation(latitude, longitude, 1);
        if (locationList != null && !locationList.isEmpty()) {
            Address address = locationList.get(0);
            String countryName = address.getCountryName();        // "China"
            String adminArea = address.getAdminArea();            // "Beijing"
            String locality = address.getLocality();              // "Chaoyang District"
            String thoroughfare = address.getThoroughfare();      // "Zhongguancun Street"
            String featureName = address.getFeatureName();        // "Building number"
            String postalCode = address.getPostalCode();          // "100000"

            // Full address line
            String fullAddress = address.getAddressLine(0);       // "123 Zhongguancun Street, Chaoyang District, Beijing, China"
            Log.d("Address", fullAddress);

            if (addressCallback != null){
                addressCallback.onGetAddress(address);
            }

        } else {
            Log.d("LocationUtil", "No address found");
        }

    }

    private void pauseRecording() {
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        stopSensorRecording();
        isRecording = false;
        remoteView.setImageViewResource(R.id.iv_StartPause, R.drawable.start_recording3);
        remoteView.setTextViewText(R.id.tv_notification_state, "记录已暂停");
        notificationManager.notify(FOREGROUND_ID, notification);

        recordingStateListener.onPauseRecording();
    }

    private void continueRecording() {
        getLocation();
        startSensorRecording();
        isRecording = true;
        remoteView.setImageViewResource(R.id.iv_StartPause, R.drawable.pause_recording3);
        remoteView.setTextViewText(R.id.tv_notification_state, "正在记录");
        notificationManager.notify(FOREGROUND_ID, notification);

        recordingStateListener.onContinueRecording();
    }

    private void stopRecording(boolean fromNotification) {
        removeLocationUpdatesListener();
        stopSensorRecording();
        clearAddressCallback();
        location = null;
        isInit = false;
        isRecording = false;
        stopForeground(true);

        // ✅ Notify fragment BEFORE stopping (gives it chance to unbind)
        if (!fromNotification) {
            if (recordingStateListener != null) {
                recordingStateListener.onStopRecording();
            }
        }

        // ✅ Give fragment a moment to unbind, then force stop
        new android.os.Handler().postDelayed(() -> {
            stopSelf();
            Log.d("LocationService", "stopSelf() called after delay");
        }, 100);
    }

    private void setRecordingStateListener(RecordingStateListener recordingStateListener) {
        this.recordingStateListener = recordingStateListener;
    }

    private String getCreatedTime() {
        return createdTime;
    }

    public RecordingDatabaseHelper getRecordingDatabaseHelper() {
        return recordingDatabaseHelper;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyLocationBinder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class MyLocationBinder extends Binder {
        private LocationService mLocationService;

        public MyLocationBinder(LocationService locationService) {
            this.mLocationService = locationService;
        }

        public void setAddressCallback(AddressCallback addressCallback) {
            mLocationService.setAddressCallback(addressCallback);
        }

        public void pauseRecording() {
            mLocationService.pauseRecording();
        }

        public void continueRecording() {
            mLocationService.continueRecording();
        }

        public void stopRecording(boolean fromNotification) {
            mLocationService.stopRecording(fromNotification);
        }

        public void setRecordingStateListener(RecordingStateListener recordingStateListener) {
            mLocationService.setRecordingStateListener(recordingStateListener);
        }

        public String getCreatedTime() {
            return mLocationService.getCreatedTime();
        }

        public RecordingDatabaseHelper getRecordingDatabaseHelper() {
            return mLocationService.getRecordingDatabaseHelper();
        }

    }

}