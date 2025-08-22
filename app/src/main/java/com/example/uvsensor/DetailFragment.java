package com.example.uvsensor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.uvsensor.bean.ConfigItem;
import com.example.uvsensor.bean.LocationType;
import com.example.uvsensor.bean.SensorType;
import com.example.uvsensor.service.utils.RecordingDatabaseHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DetailFragment extends Fragment {
    private ConfigItem item;
    private XAxis xAxis;
    private YAxis yAxis;
    private LineChart lineChart;
    private Intent infoIntent;
    private TextView tv_title, tv_type_hint, tv_type, tv_intro_hint, tv_intro, tv_attribute;
    private ImageView iv_back;
    private Timer timer;
    Runnable setData;
    private RecordingDatabaseHelper recordingDatabaseHelper;

    public DetailFragment() {
        // empty
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        initComponents(root);

        // 立即尝试执行一次数据加载，方便调试
        if (setData != null) {
            setData.run();
        }

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop timer when fragment is not visible
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restart timer when fragment becomes visible
        startTimer();
    }

    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // 以防软件一打开，home还不存在，就去call人家，这样会闪退的
                    if (((MainActivity) requireActivity()).homeFragment != null) {
                        recordingDatabaseHelper = ((MainActivity) requireActivity()).homeFragment.getDataBaseHelper();
                        setData.run();
                    }
                    else {
                        Log.d("DetailFragment: startTimer", "((MainActivity) requireActivity()).homeFragment is null");
                    }
                }
            }, 0, 2000);
        }
    }

    private void initComponents(View root) {

        Bundle args = getArguments();
        if (args != null) {
            item = (ConfigItem) args.getSerializable("item_type");
        }

        // 尝试立即初始化 recordingDatabaseHelper
        try {
            if (((MainActivity) requireActivity()).homeFragment != null) {
                recordingDatabaseHelper = ((MainActivity) requireActivity()).homeFragment.getDataBaseHelper();
                Log.d("DetailFragment", "recordingDatabaseHelper initialized successfully");
            }
        } catch (Exception e) {
            Log.e("DetailFragment", "Failed to initialize recordingDatabaseHelper: " + e.getMessage());
        }

        tv_title = root.findViewById(R.id.title_item_name);
        tv_title.setText(item.getItem_name());

        tv_type_hint = root.findViewById(R.id.item_type_hint);
        tv_type = root.findViewById(R.id.item_type);
        tv_intro_hint = root.findViewById(R.id.item_intro_hint);
        tv_intro = root.findViewById(R.id.item_intro);
        tv_attribute = root.findViewById(R.id.sqlite_attribute);

        if (item.getType() == 0) {  // item是sensor
            tv_type_hint.setText("传感器类型：");
            tv_intro_hint.setText("传感器介绍：");
            setData = this::setSensorData;  // 在初始化时为setData赋值，避免后来在timer中多次判断item的类型
        } else if (item.getType() == 1) {  // item是location
            tv_type_hint.setText("定位类型：");
            tv_intro_hint.setText("定位类型介绍：");
            setData = this::setLocationData;
        }

        iv_back = root.findViewById(R.id.back);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                requireActivity().getSupportFragmentManager().popBackStack();
//                我们直接从mainActivity中销毁这个fragment
                ((MainActivity) requireActivity()).destoryDetailFragment();
            }
        });

        lineChart = root.findViewById(R.id.chart);

        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);

        xAxis = lineChart.getXAxis();
        xAxis.setDrawAxisLine(true); // 显示X轴轴线
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // 确保X轴在底部

        yAxis = lineChart.getAxisLeft();
        yAxis.setLabelCount(5, false);
        yAxis.setDrawZeroLine(true); // 显示零线
        yAxis.setZeroLineWidth(0.3f);  // 设置零线宽度为0.3（和你的曲线一样细）

        lineChart.getAxisRight().setEnabled(true); // 启用右侧Y轴
        lineChart.getAxisRight().setDrawAxisLine(true); // 显示右侧Y轴的轴线
        lineChart.getAxisRight().setDrawGridLines(false);  // 不显示右侧网格线
        lineChart.getAxisRight().setDrawLabels(false);  // 右边轴不要显示刻度了


        startTimer();
    }

    private void setLocationData() {
        Log.d("DetailFragment", "setLocationData() called");
        ArrayList<Entry> accs = new ArrayList<>();

        if (recordingDatabaseHelper == null || item == null) {
            if (lineChart != null) {
//                lineChart.setVisibility(View.GONE);
            }
            return;
        }

        Log.d("DetailFragment", "Using table: " + item.getItem_table_name());

        try {
            SQLiteDatabase db = recordingDatabaseHelper.getReadableDatabase();
            if (db == null || !db.isOpen()) {
                Log.w("DetailFragment", "Database connection closed, skipping update");
                return;
            }

            // 只选前100个数据
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + item.getItem_table_name() + " ORDER BY time ASC LIMIT 100",
                    null
            );

            Log.d("DetailFragment", "Query executed, cursor count: " + (cursor != null ? cursor.getCount() : "null"));

            float min = Float.MAX_VALUE;
            float max = -Float.MAX_VALUE;

            if (cursor != null & cursor.moveToNext()) {  // 就是说能查到对应的表，并且表不为空
                int index = 0;
                while(cursor.moveToNext()) {
                    @SuppressLint("Range") float acc = cursor.getFloat(cursor.getColumnIndex("acc"));
                    accs.add(new Entry(index, acc));

                    min = Math.min(min, acc);
                    max = Math.max(max, acc);
                    index ++;  // 仅此++纪念c艹和年轻时的我
                }
                cursor.close();
            }

            updateChart(accs, null, null, min, max);

        } catch (IllegalStateException e) {
            Log.w("DetailFragment", "Database connection closed, skipping update");
        }
    }

    private void setSensorData() {

        ArrayList<Entry> xValues = new ArrayList<>();  // 其实之前咱用的list也都是用arraylist实例化的，所以没啥区别
        ArrayList<Entry> yValues = new ArrayList<>();
        ArrayList<Entry> zValues = new ArrayList<>();

//        这样是不够的，因为当记录暂停的时候数据库和dbHelper的连接会关上，但是dbhelper依旧存在，导致recordingDatabaseHelper.getReadableDatabase()会闪退。
        if (recordingDatabaseHelper == null || item == null) {
            if (lineChart != null) {
//                lineChart.setVisibility(View.GONE);
            }
            return;
        }


        try {
            SQLiteDatabase db = recordingDatabaseHelper.getReadableDatabase();
            if (db == null || !db.isOpen()) {
                return;
            }

            // 只选前100个数据
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(
                                                "SELECT * FROM " + item.getItem_table_name() + " ORDER BY time ASC LIMIT 100",
                                                null
            );

            float min = Float.MAX_VALUE;
            float max = -Float.MAX_VALUE;

            if (cursor != null & cursor.moveToNext()) {  // 就是说能查到对应的表，并且表不为空
                int index = 0;
                boolean isThreeAxisSensor = isThreeAxisSensor(((SensorType) item).getSensor_type());
                while(cursor.moveToNext()) {
                    if (isThreeAxisSensor) {
                        @SuppressLint("Range") float x = cursor.getFloat(cursor.getColumnIndex("x"));  // 前面@啥的是为了防止找不到对应的列名
                        @SuppressLint("Range") float y = cursor.getFloat(cursor.getColumnIndex("y"));
                        @SuppressLint("Range") float z = cursor.getFloat(cursor.getColumnIndex("z"));
                        xValues.add(new Entry(index, x));
                        yValues.add(new Entry(index, y));
                        zValues.add(new Entry(index, z));

                        min = Math.min(min, Math.min(x, Math.min(y, z)));
                        max = Math.max(max, Math.max(x, Math.max(y, z)));
                    }
                    else {  // 那就是只有一个数据
                        @SuppressLint("Range") float x = cursor.getFloat(cursor.getColumnIndex("value"));
                        xValues.add(new Entry(index, x));

                        min = Math.min(min, x);
                        max = Math.max(max, x);
                    }
                    index ++;  // 仅此++纪念c艹和年轻时的我
                }
                cursor.close();
            }

            updateChart(xValues, yValues, zValues, min, max);

        } catch (IllegalStateException e) {
            Log.w("DetailFragment", "Database connection closed, skipping update");
        }
    }

    private void updateChart(ArrayList<Entry> xValues, ArrayList<Entry> yValues, ArrayList<Entry> zValues, float ymin, float ymax) {
        // 在主线程中更新UI, 别忘了setData()是在子线程中调用的，现在让我们回归主线程
        new Handler(Looper.getMainLooper()).post(new Runnable() {  // 这个hander会把下面的代码切换到主线程中运行
            @Override
            public void run() {

                if (yAxis != null) {
                    yAxis.setAxisMinimum(Math.min(0f, ymin - 5));
                    yAxis.setAxisMaximum(ymax + 5);
//                    yAxis.setLabelCount(5, false);
                    yAxis.setMinWidth(40f); // Y轴区域最少40dp宽
                    yAxis.setMaxWidth(80f);
                }

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                if (xValues != null && !xValues.isEmpty() && yValues != null && !yValues.isEmpty() && zValues != null && !zValues.isEmpty()) {
                    LineDataSet xSet = new LineDataSet(xValues, "x");
                    xSet.setColor(Color.RED);
                    xSet.setLineWidth(3f); // 线宽设为3，默认是1
                    xSet.setDrawCircles(false);

                    LineDataSet ySet = new LineDataSet(yValues, "y");
                    ySet.setColor(Color.GREEN);
                    ySet.setLineWidth(3f); // 线宽设为3，默认是1
                    ySet.setDrawCircles(false);

                    LineDataSet zSet = new LineDataSet(zValues, "z");
                    zSet.setColor(Color.BLUE);
                    zSet.setLineWidth(3f); // 线宽设为3，默认是1
                    zSet.setDrawCircles(false);

                    dataSets.add(xSet);
                    dataSets.add(ySet);
                    dataSets.add(zSet);
                }
                else if (xValues != null && !xValues.isEmpty()) {
                    LineDataSet set = new LineDataSet(xValues, "value");
                    set.setColor(Color.BLUE);
                    set.setLineWidth(3f); // 线宽设为3，默认是1
                    set.setDrawCircles(false);
                    dataSets.add(set);
                }

                lineChart.setData(new LineData(dataSets));
                lineChart.invalidate();
//                lineChart.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isThreeAxisSensor(int type) {
        return type == Sensor.TYPE_ACCELEROMETER ||
                type == Sensor.TYPE_GYROSCOPE ||
                type == Sensor.TYPE_MAGNETIC_FIELD ||
                type == Sensor.TYPE_GRAVITY ||
                type == Sensor.TYPE_LINEAR_ACCELERATION ||
                type == Sensor.TYPE_ROTATION_VECTOR  // 这个我们暂时也只画xyz吧，w的话感觉画出来没啥意思，后续可以考虑在图标旁边显示实时数值
                ;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清理定时器
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}