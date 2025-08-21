package com.example.uvsensor;

import static android.content.Context.BIND_AUTO_CREATE;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import static com.example.uvsensor.service.utils.ColorUtil.getAccuracyByColor;
import static com.example.uvsensor.service.utils.ColorUtil.getColorByAccuracy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.example.uvsensor.data.GlobalContants;
import com.example.uvsensor.listener.AddressCallback;
import com.example.uvsensor.listener.RecordingStateListener;
import com.example.uvsensor.service.LocationService;
import com.example.uvsensor.service.utils.ColorUtil;
import com.example.uvsensor.service.utils.RecordingDatabaseHelper;
import com.example.uvsensor.service.utils.TimeUtil;
import com.example.uvsensor.service.utils.WGS84TOGCJ02;
import com.example.uvsensor.service.utils.zipFolder;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

//    private View root;
    private List<Integer> configList;
    private ActivityResultLauncher<String> locationPermission;  // 请求定位权限
    private ActivityResultLauncher<String> storagePermission;  // 请求存储权限
    boolean notificationPermission;  // 请求权限
    private MapView mMapView = null;
    private AMap aMap;
    private ImageView BtStartPause, BtStop, BtDrawTracks;
//    private TextView TxtStartPause, tv_duration;
    private TextView tv_duration;
    private LocationService.MyLocationBinder locationBinder;
//    private Polyline polyline, polyline_pseudo;
    private List<Polyline> polylines_GPS, polylines_network;
    private List<Integer> colors_GPS, colors_Network;
    private int lastColor_GPS, lastColor_network;
    private List<List<LatLng>> latLngs_GPS, latLngs_network;  // 两个都是火星坐标
    private int state = GlobalContants.NOTBEGIN;
    private Intent RecordingIntent;
    private int[] exportedFormat;  // 导出的文件的格式
    private File dir; // 我们这个项目存储轨迹记录的文件夹
    private String serviceCreatedTime;  // 初始化放在finishRecording()里面了
    private RecordingDatabaseHelper recordingDatabaseHelper;  // 初始化放在finishRecording()里面了
    private MarkerOptions markerOptions;
    private Marker marker_GPS, marker_Network;
    private com.amap.api.maps.model.Circle circle_GPS, circle_Network;
    private Timer timer;
    private Boolean needChangeMapCenter;  // 是否需要改变地图的中心点
    private long validTime;  // 就是有效记录的时长。等于总时长减去暂停的时长
    private long startTime;
    boolean compensatedForStartTime = false;  // 就是当暂停结束后，我们为 start += pause_interval，为了确保只加一次，我们引入这个布尔变量
    private List<String> locationProviders;
    private boolean showGPS;
    private boolean showNetwork;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            locationBinder = (LocationService.MyLocationBinder) iBinder;

            serviceCreatedTime = locationBinder.getCreatedTime();
            recordingDatabaseHelper = locationBinder.getRecordingDatabaseHelper();

            Log.d("HomeFragment", "onServiceConnected: serviceCreatedTime=" + serviceCreatedTime);
            Log.d("HomeFragment", "onServiceConnected: db_name=" + recordingDatabaseHelper.getDatabaseName());

            locationBinder.setAddressCallback(new AddressCallback() {
                @Override
                public void onGetAddress(Address address) {}

                @Override
                public void onGetLocation(Location location) {

                    // 从网上找的能获取更高精度的野方法
                    double lat = Double.parseDouble(String.format("%.6f", location.getLatitude()));
                    double lng = Double.parseDouble(String.format("%.6f", location.getLongitude()));
                    float acc = location.getAccuracy();
                    String src = location.getProvider();

                    // 转换为火星坐标系
                    double[] transformedLocation = WGS84TOGCJ02.wgs84ToGcj02(lat, lng);
                    lat = transformedLocation[0];
                    lng = transformedLocation[1];

                    Log.d("HomeFragment", "onGetLocation: lat=" + lat + ", lng=" + lng + ", needChangeMapCenter=" + needChangeMapCenter);

                    if (aMap != null && isAdded() && isVisible()) {

                        if (needChangeMapCenter) {
                            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 18));
                            Log.d("HomeFragment", "aMap.moveCamera called, lat=" + lat + ", lng=" + lng);
                            needChangeMapCenter = false;
                        }

                        addLocation(lat, lng, acc, src);

//                        如果用户要求显示，我们才绘制
                        if (src.equals(LocationManager.GPS_PROVIDER) && showGPS){
                            drawTracks(acc, src);
                        }
                        else if (src.equals(LocationManager.NETWORK_PROVIDER) && showNetwork) {
                            drawTracks(acc, src);
                        }

                    } else {

                        needChangeMapCenter = true;

                        addLocation(lat, lng, acc, src);
                        Log.d("HomeFragment", "Map not ready, stored location for later");
                    }
                }

//                @Override
//                public void onGetFirstLocation(Location location) {
//                    checkMapReady();
//                }
            });

            locationBinder.setRecordingStateListener(new RecordingStateListener() {
                @Override
                public void onPauseRecording() {  // 该函数被调用，说明我们现在要将记录暂停
                    BtStartPause.setImageResource(R.drawable.start_recording3);
//                    TxtStartPause.setText("继续记录");
                    state = GlobalContants.PAUSE;
                }

                @Override
                public void onContinueRecording() {  // 该函数被调用，说明我们现在要将开始记录
                    BtStartPause.setImageResource(R.drawable.pause_recording3);
//                    TxtStartPause.setText("暂停记录");
                    state = GlobalContants.RECORDING;
                }

                @Override
                public void onStopRecording() {
                    finishRecording(true);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            locationBinder = null;
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("HomeFragment", "onCreateView is called");

//        不要将root设置为全局变量，不要缓存 root，每次都重新 inflate 布局
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        initComponents(savedInstanceState, root);
        return root;
    }

    // 初始换各种组件，并为按钮添加监听
    private void initComponents(Bundle savedInstanceState, View root) {

        notificationPermission = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled();

        if (!notificationPermission) {
            // 请求通知权限
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
            startActivity(intent);
        }


        locationPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            // 权限申请结果
            Log.d(GlobalContants.TAG, "位置权限申请结果: " + result);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityResultLauncher<String> notificationPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                        Log.d(GlobalContants.TAG, "通知权限申请结果: " + result);
                        if (!result) {
                            Toast.makeText(requireContext(), "通知权限被拒绝，部分功能可能无法使用", Toast.LENGTH_SHORT).show();
                        }
                        // 通知权限弹窗结束后再请求位置权限
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    });

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // 如果通知权限已授权，直接请求位置权限
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }
        } else {
            // 低版本直接请求位置权限
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        storagePermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            // 权限申请结果
            Log.d(GlobalContants.TAG, "存储权限申请结果: " + result);
            if (result) {  // 获取权限
                try {
                    store(); // ← Only called WHEN permission result comes back。再storagePermission被初始化的时候并不会执行
                } catch (XmlPullParserException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                Toast.makeText(requireContext(), "存储权限被拒绝，无法保存文件", Toast.LENGTH_SHORT).show();
            }
        });

        needChangeMapCenter = true;

        showGPS = true;
        showNetwork = true;

        locationProviders = new ArrayList<>();

        startTime = 0;
        validTime = 0;

        configList = new ArrayList<>();

        mMapView = (MapView) root.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

//        latLngs = new ArrayList<LatLng>();
        latLngs_GPS = new ArrayList<>();
        latLngs_network = new ArrayList<>();

        polylines_GPS = new ArrayList<>();
        polylines_network = new ArrayList<>();

        colors_GPS = new ArrayList<>();
        colors_Network = new ArrayList<>();

        lastColor_GPS = -1;
        lastColor_network = -1;

        exportedFormat = new int[]{0};

        BtStartPause = root.findViewById(R.id.home_BTStartPause);
//        TxtStartPause = root.findViewById(R.id.home_text_StartPauseRecording);
        tv_duration = root.findViewById(R.id.time);
        BtStop = root.findViewById(R.id.home_BTStop);
        BtDrawTracks = root.findViewById(R.id.draw_option);

        markerOptions = new MarkerOptions();


        BtStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (state) {
                    case GlobalContants.NOTBEGIN:
                        BtStartPause.setImageResource(R.drawable.pause_recording3);
                        state = GlobalContants.RECORDING;
                        startRecordingService();
//                        TxtStartPause.setText("开始记录");
                        break;
                    case GlobalContants.PAUSE:
                        locationBinder.continueRecording();
                        BtStartPause.setImageResource(R.drawable.pause_recording3);
//                        TxtStartPause.setText("暂停记录");
                        state = GlobalContants.RECORDING;
                        break;
                    case GlobalContants.RECORDING:
                        locationBinder.pauseRecording();
                        BtStartPause.setImageResource(R.drawable.start_recording3);
//                        TxtStartPause.setText("继续记录");
                        state = GlobalContants.PAUSE;
                        break;
                }
            }
        });

        BtStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {finishRecording(false);}
        });

        BtDrawTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseDrawingTrack();
            }
        });

        checkMapReady();

    }

    private void chooseDrawingTrack() {

        Log.d("HomeFragment", "showGPS is " + showGPS);
        Log.d("HomeFragment", "showNetwork is " + showNetwork);

        if (locationProviders == null || locationProviders.isEmpty()) {
            Toast.makeText(requireContext(), "记录未开始", Toast.LENGTH_SHORT).show();
            return;
        }

//        Dialog dialog = new Dialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.draw_option_dialog_layout, null);

        CheckBox cbShowGPS = dialogView.findViewById(R.id.cb_showGPS);
        CheckBox cbShowNetwork = dialogView.findViewById(R.id.cb_showNetwork);

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {

            if (buttonView == cbShowGPS) {
                showGPS = isChecked;

                if (isChecked) {
                    if (!colors_GPS.isEmpty()) {
                        float avg_acc = getAccuracyByColor(colors_GPS.get(colors_GPS.size() - 1));
                        drawTracks(avg_acc, LocationManager.GPS_PROVIDER);
                    } else {
//                        Toast.makeText(requireContext(), "暂无轨迹记录，请稍后重试", Toast.LENGTH_SHORT).show();
//                        cbShowGPS.setChecked(false);
                    }
                }
                else {
                    clearTracks(LocationManager.GPS_PROVIDER);
                }
            }
            if (buttonView == cbShowNetwork) {
                showNetwork = isChecked;

                if (isChecked) {
                    if (!colors_Network.isEmpty()) {
                        float avg_acc = getAccuracyByColor(colors_Network.get(colors_Network.size() - 1));
                        drawTracks(avg_acc, LocationManager.NETWORK_PROVIDER);
                    } else {  // 如果没轨迹的话我们就霸道地取消掉用户的勾选吧。哪天良心发现的话就把这个逻辑完善一下。
//                        Toast.makeText(requireContext(), "暂无轨迹记录，请稍后重试", Toast.LENGTH_SHORT).show();
//                        cbShowNetwork.setChecked(false);
                    }
                } else {
                    clearTracks(LocationManager.NETWORK_PROVIDER);
                }
            }

        };

        // 因为该popupwindow会多次初始化。用这两个变量来让它不要忘记之前的设定
        // 又，我们默认两个按钮都是打开的（在xml写的），所以我们只需要判断要不要把它们关上就行
        if (!showGPS) {
            cbShowGPS.setChecked(false);
        }
        if (!showNetwork) {
            cbShowNetwork.setChecked(false);
        }

        // 如果按钮已经不可点击，那监听等于没设置
        cbShowGPS.setOnCheckedChangeListener(listener);
        cbShowNetwork.setOnCheckedChangeListener(listener);

        // 提醒一下，不要忘了showGPS和showNetwork这两个布尔变量的对应调整啊
        if (!locationProviders.contains((LocationManager.GPS_PROVIDER))) {
            cbShowGPS.setChecked(false);
            cbShowGPS.setEnabled(false); // 不可点击
            cbShowGPS.setTextColor(Color.GRAY); // 文本变灰
        }
        else if (!locationProviders.contains((LocationManager.NETWORK_PROVIDER))) {
            cbShowNetwork.setChecked(false);
            cbShowNetwork.setEnabled(false);
            cbShowGPS.setTextColor(Color.GRAY);
        }

        PopupWindow popupWindow = new PopupWindow(dialogView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);

        // 设置背景
        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.WHITE));
        popupWindow.setOutsideTouchable(true);

        // 让弹窗贴着 BtDrawTracks 显示
        dialogView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = dialogView.getMeasuredHeight();
        popupWindow.showAsDropDown(BtDrawTracks, 0, -popupHeight - 100);
    }

    public RecordingDatabaseHelper getDataBaseHelper() {
        return recordingDatabaseHelper;
    }

    private void updateTimerDisplay() {
        // 这里的逻辑不能和比方说BtStartPause.setOnClickListener(new View.OnClickListener()一样，因为那边是要调整状态，但是我们这边check到是什么状态就是什么状态，改不了
        switch (state) {
            case GlobalContants.NOTBEGIN:
                tv_duration.setText(TimeUtil.mill2Time(0));
                break;
            case GlobalContants.PAUSE:
                compensatedForStartTime = false;  // 防止多次暂停但compensatedForStartTime不更新的情况。所以只要处于暂停状态，我们就把它设为false
                break;
            case GlobalContants.RECORDING:
                if (compensatedForStartTime == false){
                    startTime = System.currentTimeMillis()- validTime;
                    compensatedForStartTime = true;
                }
                validTime = System.currentTimeMillis() - startTime;
                String tv_duration_text = TimeUtil.mill2Time(validTime);
                tv_duration.setText(tv_duration_text);
                break;
        }
    }


    // 混合的方式创建service
    private void startRecordingService() {
        locationProviders = ((MainActivity) requireActivity()).configFragment.getLocationConfigList();

        if (locationProviders.isEmpty()) {
            BtStartPause.setImageResource(R.drawable.start_recording3);
            state = GlobalContants.NOTBEGIN;
            Toast.makeText(getContext(), "请至少选择一种定位方式", Toast.LENGTH_SHORT).show();
            return;
        }

        configList = ((MainActivity) requireActivity()).configFragment.getSensorConfigList();

//        Log.d("HomeFragment:StartRecordingService", "configList is " + configList);
//        Log.d("HomeFragment:StartRecordingService", "locationProviders is " + locationProviders);

        RecordingIntent = new Intent(getContext(), LocationService.class);
        RecordingIntent.putIntegerArrayListExtra("configList", (ArrayList<Integer>) configList);
        RecordingIntent.putStringArrayListExtra("locationProvider", (ArrayList<String>) locationProviders);

        requireContext().bindService(RecordingIntent, conn, BIND_AUTO_CREATE);
        requireContext().startService(RecordingIntent);

        Log.d("HomeFragment", "startRecordingService: called, waiting for serviceCreatedTime update...");

        validTime = 0;
        startTime = System.currentTimeMillis();
        startTimer();
    }

    // 计算记录的时长并显示在页面上
    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 因为timer没办法修改UI，很麻烦，所以我们直接让它调用一个函数去搞
                            updateTimerDisplay();
                        }
                    });
                }
            }, 0, 1000);
        }

    }

    //    当页面可见时初始化地图，否则就过会儿继续check
    private void checkMapReady() {

        if (mMapView != null && isAdded() && isVisible()) {  // isAdded() && isVisible() 是  androidx.fragment.app.Fragment 包中的，所以你可以猜出来它们是干什么的
            if (aMap == null) {
                aMap = mMapView.getMap();
            }
//
//            // 改变地图中心点
//            if (location != null) {
//                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
//            }

            // 获取地图UI设置
            UiSettings uiSettings = aMap.getUiSettings();
            // 设置缩放控件到右边中间
            uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);

            // 根据按钮状态绘制轨迹
            if (aMap != null && isAdded() && isVisible()) {
                if (showGPS && !latLngs_GPS.isEmpty()) {
                    float avg_acc = getAccuracyByColor(colors_GPS.get(colors_GPS.size()-1));
                    drawTracks(avg_acc, LocationManager.GPS_PROVIDER);
                }
                if (showNetwork && !latLngs_network.isEmpty()) {
                    float avg_acc = getAccuracyByColor(colors_Network.get(colors_Network.size()-1));
                    drawTracks(avg_acc, LocationManager.NETWORK_PROVIDER);
                }
            }
        } else {
            new android.os.Handler().postDelayed(() -> {
                if (isAdded() && isVisible()) {
                    checkMapReady();
                }
            }, 500);
        }
    }

    private void addLocation(double lat, double lng, float acc, String src) {

        // 下面的实现全部基于GPS
        LatLng latLng = new LatLng(lat, lng);
        int currentColor = getColorByAccuracy(acc, src);

        if (src.equals(LocationManager.GPS_PROVIDER)) {
            if (lastColor_GPS != currentColor || lastColor_GPS == -1) {  // 精度变化：第一个数据点，或者后续的数据点和先前的精度不同
                List<LatLng> latLngs = new ArrayList<>();

                // 在分段时把上一段最后一个点加到新段开头，这样视觉上轨迹不会断开
                if (!latLngs_GPS.isEmpty() && lastColor_GPS != -1) {
                    latLngs.add(latLngs_GPS.get(latLngs_GPS.size()-1).get(  // （可读性极低）其实只是拿到latLngs_GPS的最后一个列表的最后一个元素，并加入到当前列表，不然我觉得那个polyline可能会断开，这样只是为了视觉上的合理性
                            latLngs_GPS.get(latLngs_GPS.size()-1).size() - 1
                    ));
                }

                latLngs.add(latLng);
                latLngs_GPS.add(latLngs);
                colors_GPS.add(currentColor);

            }
            else if (lastColor_GPS == currentColor) {  // 精度不变
                // 为latLngs_GPS这个二维列表的最后一个列表元素加上新的经纬度
                latLngs_GPS.get(latLngs_GPS.size() - 1).add(latLng);
            }

            lastColor_GPS = currentColor;
        }
        else if (src.equals(LocationManager.NETWORK_PROVIDER)) {
            if (lastColor_network != currentColor || lastColor_network == -1) {  // 精度变化
                List<LatLng> latLngs = new ArrayList<>();

                if (!latLngs_network.isEmpty() && lastColor_network != -1) {
                    latLngs.add(latLngs_network.get(latLngs_network.size()-1).get(
                            latLngs_network.get(latLngs_network.size()-1).size()-1
                    ));
                }

                latLngs.add(latLng);
                latLngs_network.add(latLngs);
                colors_Network.add(currentColor);
            }
            else if (lastColor_network == currentColor) {  // 精度不变
                latLngs_network.get(latLngs_network.size()-1).add(latLng);
            }

            lastColor_network = currentColor;
        }
    }

    private void clearTracks(String src) {
        if (src.equals(LocationManager.GPS_PROVIDER)) {
            for (Polyline polyline : polylines_GPS) {
                if (polyline != null) {
                    polyline.remove();
                    polyline = null;
                }
            }
            polylines_GPS.clear();
            if (marker_GPS != null) {
                marker_GPS.remove();
                marker_GPS = null;
            }
            if (circle_GPS != null) {
                circle_GPS.remove();
                circle_GPS = null;
            }
        } else if (src.equals(LocationManager.NETWORK_PROVIDER)) {
            for (Polyline polyline : polylines_network) {
                if (polyline != null) {
                    polyline.remove();
                    polyline = null;
                }
            }
            polylines_network.clear();
            if (marker_Network != null) {
                marker_Network.remove();
                marker_Network = null;
            }
            if (circle_Network != null) {
                circle_Network.remove();
                circle_Network = null;
            }

        }
    }

    private void drawTracks(float acc, String src) {

        LatLng lastLatLng = null;

        // 虽然但是我看一模一样的两段代码也会觉得恶心。想不到什么好办法了，毕竟不同的数据要单独存储
        if (src.equals(LocationManager.GPS_PROVIDER)) {// 下面的实现全部基于GPS

            if (!latLngs_GPS.isEmpty() && !latLngs_GPS.get(latLngs_GPS.size()-1).isEmpty()) {
                lastLatLng = latLngs_GPS.get(latLngs_GPS.size()-1)
                        .get(latLngs_GPS.get(latLngs_GPS.size()-1).size() - 1);
            }
            else {
//                Log.d("HomeFragment:drawTracks", "no location is recorded in GPS list");
            }

            int start = polylines_GPS.isEmpty() ? 0 : polylines_GPS.size() - 1;  // 我们不光要补齐完全没有绘制的latlngs列表，还得把最后一个画好的polyline清空重画，因为最后一个可能并没有画完对应latlngs里所有的点（感觉我是天才）
            int difference = latLngs_GPS.size() - start;

            if (!polylines_GPS.isEmpty()) {
                polylines_GPS.get(start).remove();  // 好了把最后一个清空了，我们可以开始drawing了
                polylines_GPS.remove(start);
            }

            for (int idx = start; idx < difference + start; idx++) {
                Polyline polyline = aMap.addPolyline(new PolylineOptions()
                        .addAll(latLngs_GPS.get(idx))
                        .width(10)
                        .color(colors_GPS.get(idx)));
                polylines_GPS.add(polyline);
            }

            if (marker_GPS != null) {
                marker_GPS.remove();
            }  // 如果marker不为空的话清除marker，我们只显示最新位置的marker就好
            if (circle_GPS != null) {
                circle_GPS.remove();
            }

            marker_GPS = aMap.addMarker(markerOptions.position(lastLatLng));

            int currentColor = polylines_GPS.get(polylines_GPS.size() - 1).getColor();
            circle_GPS = aMap.addCircle(new CircleOptions()
                    .center(lastLatLng)
                    .radius(acc) // 单位米
                    .strokeColor(currentColor)
                    .fillColor(ColorUtil.adjustAlpha(currentColor, 0.2f))); // 半透明填充
        }

        else if (src.equals(LocationManager.NETWORK_PROVIDER)) { // 下面的实现全部基于Network

            if (!latLngs_network.isEmpty() && !latLngs_network.get(latLngs_network.size()-1).isEmpty()) {
                lastLatLng = latLngs_network.get(latLngs_network.size()-1)
                        .get(latLngs_network.get(latLngs_network.size()-1).size() - 1);
            } else {
                Log.d("HomeFragment:drawTracks", "no location is recorded in Network list");
            }

            int start = polylines_network.isEmpty() ? 0 : polylines_network.size() - 1;
            int difference = latLngs_network.size() - start;

            if (!polylines_network.isEmpty()) {
                polylines_network.get(start).remove();  // 清空最后一个polyline
                polylines_network.remove(start);
            }

            for (int idx = start; idx < difference + start; idx++) {
                Polyline polyline = aMap.addPolyline(new PolylineOptions()
                        .addAll(latLngs_network.get(idx))
                        .width(10)
                        .color(colors_Network.get(idx)));
                polylines_network.add(polyline);
            }

            if (marker_Network != null) {
                marker_Network.remove();
            }
            if (circle_Network != null) {
                circle_Network.remove();
            }

            marker_Network = aMap.addMarker(markerOptions.position(lastLatLng));

            int currentColor = polylines_network.get(polylines_network.size() - 1).getColor();
            circle_Network = aMap.addCircle(new CircleOptions()
                    .center(lastLatLng)
                    .radius(acc)
                    .strokeColor(currentColor)
                    .fillColor(ColorUtil.adjustAlpha(currentColor, 0.2f)));
        }

    }

    public void finishRecording (boolean fromNotification) {
        if (state != GlobalContants.NOTBEGIN) {
            AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(requireContext());
            alertdialogbuilder.setMessage("您确认要结束记录吗？");
            alertdialogbuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    for (String locationProvider: locationProviders) {
                        clearTracks(locationProvider);
                    }

                    if (!latLngs_GPS.isEmpty()) {
                        latLngs_GPS.clear();
                    }
                    if (!latLngs_network.isEmpty()) {
                        latLngs_network.clear();
                    }

                    colors_GPS.clear();
                    colors_Network.clear();

                    lastColor_GPS = -1;  // 重置为初始值
                    lastColor_network = -1;

                    showGPS = true;
                    showNetwork = true;

                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                        startTime = 0;
                        validTime = 0;
                    }

                    exportRecording();

                    tv_duration.setText(TimeUtil.mill2Time(0));
//                    TxtStartPause.setText("开始记录");
                    BtStartPause.setImageResource(R.drawable.start_recording3);
                    state = GlobalContants.NOTBEGIN;

                    // Service 清理延迟到用户确认导出后进行
                    // cleanupService() 会在 exportRecording() 的回调中调用

                }

            });

            alertdialogbuilder.setNeutralButton("取消", null);

            final AlertDialog alertdialog1 = alertdialogbuilder.create();
            alertdialog1.show();

        } else if (!fromNotification){
            Toast.makeText(requireContext(), "记录未开始", Toast.LENGTH_SHORT).show();
        }
    }

    // 确认结束记录，进行后续导出记录等操作
    private void exportRecording() {

        String[] choices = new String[]{"导出数据", "不导出数据"};

        // 让用户选择导出数据位置的弹窗
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("请选择是否导出记录的数据")
                .setIcon(R.drawable.rui_logo)
                .setSingleChoiceItems(choices, exportedFormat[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exportedFormat[0] = i;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (exportedFormat[0] == 1) {
                            // 用户选择不导出，直接清理 Service
                            cleanupService(false);
                            return;
                        }
                        // 弹出设置存储文件位置的弹窗
                        try {
                            store();
//                            cleanupService(false);
                        } catch (XmlPullParserException | IOException e) {
                            Log.e("HomeFragment", "导出异常: " + e.getMessage());
                            cleanupService(false);
                            throw new RuntimeException(e);
                        }
                    }
                })
                .setCancelable(false)  // 走到这一步真的没有路了，记录已经结束了，要么保存，不保存的话啥都没了
                .create();

        alertDialog.show();
    }

    // 让用户设置文件存储位置，并根据选择的保存文件的格式进行文件存储。
    private void store() throws XmlPullParserException, IOException {
        // 检查存储权限
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            Log.d("HomeFragment", "存储权限已开启");

            // 创建一个UVSensor文件夹来专门存储我们的轨迹记录
            if (dir == null) {
                if (!TextUtils.isEmpty(serviceCreatedTime)) {
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyyMMdd-HH-mm-ss", Locale.getDefault());  // "T" simply separates DATE from TIME, means "Time follows"
                    isoFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                    dir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), String.format("UVSensors/%s", isoFormat.format(new Date(Long.parseLong(serviceCreatedTime)))));
                }
            }

            if (exportedFormat[0] == 0) {

                if (recordingDatabaseHelper == null) {
                    Log.e("HomeFragment", "recordingDatabaseHelper 为空，导出失败");
                    return;
                }

                boolean csvResult = recordingDatabaseHelper.exportTableToCSV(dir.getAbsolutePath());
                boolean kmlResult = recordingDatabaseHelper.exportAllLocationKML(dir.getAbsolutePath());

//                文件夹写好了，让我们来压缩一下
                String zipFilePath = dir.getAbsolutePath() + ".zip";
                new zipFolder(dir.getAbsolutePath(), zipFilePath);
                Toast.makeText(requireContext(), "已保存到" + zipFilePath, Toast.LENGTH_LONG).show();

            }

            // 清理 Service
            cleanupService(false);

        } else {
            Log.w("HomeFragment", "存储权限未开启");
            Toast.makeText(requireContext(), "存储权限未开启，请开启权限", Toast.LENGTH_LONG).show();
            storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    // 当fragment恢复时，将地图显示
    @Override
    public void onResume() {
        super.onResume();
        Log.d("HomeFragment", "onResume is called");
        Log.d("HomeFragment", "onResume is called, needChangeMapCenter=" + needChangeMapCenter);
        mMapView.onResume();  // 重新绘制加载地图
        if (aMap == null) {
            checkMapReady();
        }
        // 这一行是必要的，不然很可能resume之后并不改变中心点
        needChangeMapCenter = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    private void cleanupService(boolean fromNotification) {
        Log.d("HomeFragment", "开始清理 Service");
        
        // Tell service to stop
        if (locationBinder != null) {
            locationBinder.stopRecording(fromNotification);
            serviceCreatedTime = null;
            recordingDatabaseHelper = null;
        }

        // CRITICAL: Unbind from service so it can actually die
        if (locationBinder != null) {
            try {
                requireContext().unbindService(conn);
                locationBinder = null;
                serviceCreatedTime = null;
                recordingDatabaseHelper = null;
                dir = null;
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("HomeFragment", "onDestory is called");
        super.onDestroy();
        mMapView.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}