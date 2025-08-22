package com.example.uvsensor.service.utils;

import static com.example.uvsensor.data.GlobalContants.ACCELEROMETER_TABLE;
import static com.example.uvsensor.data.GlobalContants.ACCELEROMETER_UNCALIBRATED_TABLE;
import static com.example.uvsensor.data.GlobalContants.AMBIENT_TEMPERATURE_TABLE;
import static com.example.uvsensor.data.GlobalContants.GRAVITY_TABLE;
import static com.example.uvsensor.data.GlobalContants.GYROSCOPE_TABLE;
import static com.example.uvsensor.data.GlobalContants.GYROSCOPE_UNCALIBRATED_TABLE;
import static com.example.uvsensor.data.GlobalContants.LIGHT_TABLE;
import static com.example.uvsensor.data.GlobalContants.LINEAR_ACCELERATION_TABLE;
import static com.example.uvsensor.data.GlobalContants.LOCATION_GPS_TABLE;
import static com.example.uvsensor.data.GlobalContants.LOCATION_NETWORK_TABLE;
import static com.example.uvsensor.data.GlobalContants.MAGNETIC_FIELD_TABLE;
import static com.example.uvsensor.data.GlobalContants.PRESSURE_TABLE;
import static com.example.uvsensor.data.GlobalContants.PROXIMITY_TABLE;
import static com.example.uvsensor.data.GlobalContants.RELATIVE_HUMIDITY_TABLE;
import static com.example.uvsensor.data.GlobalContants.ROTATION_VECTOR_TABLE;
import static com.example.uvsensor.data.GlobalContants.STEP_COUNTER_TABLE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.SensorEvent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.example.uvsensor.data.GlobalContants;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class RecordingDatabaseHelper extends SQLiteOpenHelper {

    private String createdTime;
    private String db_name = "recordingDatabase_" + createdTime +".db";
    private static final int DB_VERSION = 1; // Increment version for schema changes
    
    // 表名常量
    private static final String CREATE_LATLNG_TABLE_SQL = " (time text primary key, lat real, lng real, acc real)";
    // 3轴传感器表创建SQL (x, y, z)
    private static final String CREATE_3_AXIS_TABLE_SQL = " (time text primary key, rough_time text, x real, y real, z real)";
    
    // 单值传感器表创建SQL (value)
    private static final String CREATE_SINGLE_VALUE_TABLE_SQL = " (time text primary key, rough_time text, value real)";
    
    // 4值传感器表创建SQL (x, y, z, w) - 用于旋转矢量
    private static final String CREATE_4_VALUE_TABLE_SQL = " (time text primary key, rough_time text, x real, y real, z real, w real)";

    // 6值传感器表创建accelerometer SQL (x_without_bias_compensation, y_without_bias_compensation, z_without_bias_compensation, x_with_estimated_bias_compensation, y_with_estimated_bias_compensation, z_with_estimated_bias_compensation
    private static final String CREATE_6_VALUE_ACCELEROMETER_TABLE_SQL = " (time text primary key, rough_time text, x_without_bias_compensation real, y_without_bias_compensation real, " +
            "z_without_bias_compensation real, x_with_estimated_bias_compensation real, y_with_estimated_bias_compensation real, z_with_estimated_bias_compensation real)";

    // 6值传感器表创建gyroscope SQL (x_without_bias_compensation, y_without_bias_compensation, z_without_bias_compensation, x_with_estimated_bias_compensation, y_with_estimated_bias_compensation, z_with_estimated_bias_compensation
    private static final String CREATE_6_VALUE_GYROSCOPE_TABLE_SQL = " (time text primary key, rough_time text, x_without_drift_compensation real, y_without_drift_compensation real, " +
            "z_without_drift_compensation real, estimated_x_drift_compensation real, estimated_y_drift_compensation real, estimated_z_drift_compensation real)";

    // 创建表的SQL语句
    private static final String CREATE_LOCATION_GPS_SQL = "create table " + LOCATION_GPS_TABLE + CREATE_LATLNG_TABLE_SQL;
    private static final String CREATE_LOCATION_NETWORK_SQL = "create table " + LOCATION_NETWORK_TABLE + CREATE_LATLNG_TABLE_SQL;
    private static final String CREATE_ACCELEROMETER_SQL = "create table " + ACCELEROMETER_TABLE + CREATE_3_AXIS_TABLE_SQL;
    private static final String CREATE_ACCELEROMETER_UNCALIBRATED_SQL = "create table " + ACCELEROMETER_UNCALIBRATED_TABLE + CREATE_6_VALUE_ACCELEROMETER_TABLE_SQL;
    private static final String CREATE_GYROSCOPE_SQL = "create table " + GYROSCOPE_TABLE + CREATE_3_AXIS_TABLE_SQL;
    private static final String CREATE_GYROSCOPE_UNCALIBRATED_SQL = "create table " + GYROSCOPE_UNCALIBRATED_TABLE + CREATE_6_VALUE_GYROSCOPE_TABLE_SQL;
    private static final String CREATE_GRAVITY_SQL = "create table " + GRAVITY_TABLE + CREATE_3_AXIS_TABLE_SQL;
    private static final String CREATE_LINEAR_ACCELERATION_SQL = "create table " + LINEAR_ACCELERATION_TABLE + CREATE_3_AXIS_TABLE_SQL;
    private static final String CREATE_ROTATION_VECTOR_SQL = "create table " + ROTATION_VECTOR_TABLE + CREATE_4_VALUE_TABLE_SQL;
    private static final String CREATE_MAGNETIC_FIELD_SQL = "create table " + MAGNETIC_FIELD_TABLE + CREATE_3_AXIS_TABLE_SQL;
//    private static final String CREATE_ORIENTATION_SQL = "create table " + ORIENTATION_TABLE + CREATE_3_AXIS_TABLE_SQL;
    private static final String CREATE_LIGHT_SQL = "create table " + LIGHT_TABLE + CREATE_SINGLE_VALUE_TABLE_SQL;
    private static final String CREATE_PRESSURE_SQL = "create table " + PRESSURE_TABLE + CREATE_SINGLE_VALUE_TABLE_SQL;
    private static final String CREATE_PROXIMITY_SQL = "create table " + PROXIMITY_TABLE + CREATE_SINGLE_VALUE_TABLE_SQL;
    private static final String CREATE_AMBIENT_TEMPERATURE_SQL = "create table " + AMBIENT_TEMPERATURE_TABLE + CREATE_SINGLE_VALUE_TABLE_SQL;
    private static final String CREATE_RELATIVE_HUMIDITY_SQL = "create table " + RELATIVE_HUMIDITY_TABLE + CREATE_SINGLE_VALUE_TABLE_SQL;
//    private static final String CREATE_TEMPERATURE_SQL = "create table " + TEMPERATURE_TABLE + CREATE_SINGLE_VALUE_TABLE_SQL;
    private static final String CREATE_STEP_COUNTER_SQL = "create table " + STEP_COUNTER_TABLE + CREATE_SINGLE_VALUE_TABLE_SQL;

    public RecordingDatabaseHelper(Context context, String createdTime) {
        super(context, "recordingDatabase_" + createdTime + ".db", null, DB_VERSION);
        this.createdTime = createdTime;
        this.db_name = "recordingDatabase_" + createdTime + ".db";
    }

    public String getDatabaseName() {
        return db_name;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 创建所有传感器表
        sqLiteDatabase.execSQL(CREATE_LOCATION_GPS_SQL);
        sqLiteDatabase.execSQL(CREATE_LOCATION_NETWORK_SQL);
        sqLiteDatabase.execSQL(CREATE_ACCELEROMETER_SQL);
        sqLiteDatabase.execSQL(CREATE_ACCELEROMETER_UNCALIBRATED_SQL);
        sqLiteDatabase.execSQL(CREATE_GYROSCOPE_SQL);
        sqLiteDatabase.execSQL(CREATE_GYROSCOPE_UNCALIBRATED_SQL);
        sqLiteDatabase.execSQL(CREATE_GRAVITY_SQL);
        sqLiteDatabase.execSQL(CREATE_LINEAR_ACCELERATION_SQL);
        sqLiteDatabase.execSQL(CREATE_ROTATION_VECTOR_SQL);
        sqLiteDatabase.execSQL(CREATE_MAGNETIC_FIELD_SQL);
//        sqLiteDatabase.execSQL(CREATE_ORIENTATION_SQL);
        sqLiteDatabase.execSQL(CREATE_LIGHT_SQL);
        sqLiteDatabase.execSQL(CREATE_PRESSURE_SQL);
        sqLiteDatabase.execSQL(CREATE_PROXIMITY_SQL);
        sqLiteDatabase.execSQL(CREATE_AMBIENT_TEMPERATURE_SQL);
        sqLiteDatabase.execSQL(CREATE_RELATIVE_HUMIDITY_SQL);
//        sqLiteDatabase.execSQL(CREATE_TEMPERATURE_SQL);
        sqLiteDatabase.execSQL(CREATE_STEP_COUNTER_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // 删除旧表并重新创建
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LOCATION_GPS_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LOCATION_NETWORK_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ACCELEROMETER_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ACCELEROMETER_UNCALIBRATED_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GYROSCOPE_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GYROSCOPE_UNCALIBRATED_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GRAVITY_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LINEAR_ACCELERATION_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ROTATION_VECTOR_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MAGNETIC_FIELD_TABLE);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ORIENTATION_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LIGHT_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PRESSURE_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PROXIMITY_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AMBIENT_TEMPERATURE_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RELATIVE_HUMIDITY_TABLE);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TEMPERATURE_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + STEP_COUNTER_TABLE);
        onCreate(sqLiteDatabase);
    }

    // ==================== 插入方法 ====================

    // 6轴传感器数据插入方法
    public long insertUncalibratedAccelerometerData(SensorEvent sensorEvent) {
        ContentValues values = new ContentValues();
//        values.put("time", getCurrentTime.int2readable(sensorEvent.timestamp / 1_000_000));
        values.put("time", String.valueOf(sensorEvent.timestamp));
        values.put("rough_time", getCurrentTime.int2readable(new Date().getTime()));
//        values.put("time", getCurrentTime.get());
        values.put("x_without_bias_compensation", sensorEvent.values[0]);
        values.put("y_without_bias_compensation", sensorEvent.values[1]);
        values.put("z_without_bias_compensation", sensorEvent.values[2]);
        values.put("x_with_estimated_bias_compensation", sensorEvent.values[3]);
        values.put("y_with_estimated_bias_compensation", sensorEvent.values[4]);
        values.put("z_with_estimated_bias_compensation", sensorEvent.values[5]);

        addToBatch(ACCELEROMETER_UNCALIBRATED_TABLE, values);
        return 1; // Success indicator (actual row ID not available in batch mode)
    }

    public long insertUncalibratedGyroscopeData(SensorEvent sensorEvent) {
        ContentValues values = new ContentValues();
//        values.put("time", getCurrentTime.int2readable(sensorEvent.timestamp / 1_000_000));
        values.put("time", String.valueOf(sensorEvent.timestamp));
        values.put("rough_time", getCurrentTime.int2readable(new Date().getTime()));
//        values.put("time", getCurrentTime.get());
        values.put("x_without_drift_compensation", sensorEvent.values[0]);
        values.put("y_without_drift_compensation", sensorEvent.values[1]);
        values.put("z_without_drift_compensation", sensorEvent.values[2]);
        values.put("estimated_x_drift_compensation", sensorEvent.values[3]);
        values.put("estimated_y_drift_compensation", sensorEvent.values[4]);
        values.put("estimated_z_drift_compensation", sensorEvent.values[5]);

        addToBatch(GYROSCOPE_UNCALIBRATED_TABLE, values);
        return 1; // Success indicator (actual row ID not available in batch mode)
    }

    // 3轴传感器数据插入方法
    public long insertAccelerometerData(SensorEvent sensorEvent) {
        return insert3AxisData(ACCELEROMETER_TABLE, sensorEvent);
    }

    public long insertGyroscopeData(SensorEvent sensorEvent) {
        return insert3AxisData(GYROSCOPE_TABLE, sensorEvent);
    }

    public long insertGravityData(SensorEvent sensorEvent) {
        return insert3AxisData(GRAVITY_TABLE, sensorEvent);
    }

    public long insertLinearAccelerationData(SensorEvent sensorEvent) {
        return insert3AxisData(LINEAR_ACCELERATION_TABLE, sensorEvent);
    }

    public long insertMagneticFieldData(SensorEvent sensorEvent) {
        return insert3AxisData(MAGNETIC_FIELD_TABLE, sensorEvent);
    }

    public long insertLocationData(Location location) {
        ContentValues values = new ContentValues();
//        values.put("time", getCurrentTime.get());
//        values.put("time", new Date().getTime());  // 用长整型吧
        values.put("time", getCurrentTime.locationTime(new Date().getTime()));
//        values.put("time", String.valueOf(sensorEvent.timestamp));
        values.put("lat", location.getLatitude());
        values.put("lng", location.getLongitude());
        values.put("acc", location.getAccuracy());

        String tableName = null;
        String locationProvider = location.getProvider();
        if (LocationManager.GPS_PROVIDER.equals(locationProvider)) {
            tableName = GlobalContants.LOCATION_GPS_TABLE;
        } else if (LocationManager.NETWORK_PROVIDER.equals(locationProvider)) {
            tableName = GlobalContants.LOCATION_NETWORK_TABLE;
        }

        addToBatch(tableName, values);
        return 1;
    }

    private long insertSingleValueData(String tableName, SensorEvent sensorEvent) {
        ContentValues values = new ContentValues();
//        values.put("time", getCurrentTime.int2readable(sensorEvent.timestamp / 1_000_000));
        values.put("time", String.valueOf(sensorEvent.timestamp));
        values.put("rough_time", getCurrentTime.int2readable(new Date().getTime()));
        values.put("value", sensorEvent.values[0]);

        addToBatch(tableName, values);
        return 1; // Success indicator (actual row ID not available in batch mode)
    }

    // 3轴数据插入辅助方法 - OPTIMIZED with queue batching
    private long insert3AxisData(String tableName, SensorEvent sensorEvent) {
        ContentValues values = new ContentValues();
//        values.put("time", getCurrentTime.int2readable(new Date().getTime()));
//        values.put("time", getCurrentTime.int2readable(sensorEvent.timestamp / 1_000_000));
        values.put("time", String.valueOf(sensorEvent.timestamp));
        values.put("rough_time", getCurrentTime.int2readable(new Date().getTime()));
//        values.put("time", getCurrentTime.get());
        values.put("x", sensorEvent.values[0]);
        values.put("y", sensorEvent.values[1]);
        values.put("z", sensorEvent.values[2]);

        addToBatch(tableName, values);
        return 1; // Success indicator (actual row ID not available in batch mode)
    }

    // 单值数据插入辅助方法 - OPTIMIZED with queue batching

    // 旋转矢量数据插入方法 (4个值) - OPTIMIZED with queue batching
    public long insertRotationVectorData(SensorEvent sensorEvent) {
        ContentValues values = new ContentValues();
//        values.put("time", getCurrentTime.int2readable(sensorEvent.timestamp / 1_000_000));
        values.put("time", String.valueOf(sensorEvent.timestamp));
        values.put("rough_time", getCurrentTime.int2readable(new Date().getTime()));
//        values.put("time", getCurrentTime.get());
        values.put("x", sensorEvent.values[0]);
        values.put("y", sensorEvent.values[1]);
        values.put("z", sensorEvent.values[2]);
        if (sensorEvent.values.length > 3) {
            values.put("w", sensorEvent.values[3]);
        } else {
            values.put("w", 0.0f);
        }

        addToBatch(ROTATION_VECTOR_TABLE, values);
        return 1; // Success indicator (actual row ID not available in batch mode)
    }

    // 单值传感器数据插入方法
    public long insertLightData(SensorEvent sensorEvent) {
        return insertSingleValueData(LIGHT_TABLE, sensorEvent);
    }

    public long insertPressureData(SensorEvent sensorEvent) {
        return insertSingleValueData(PRESSURE_TABLE, sensorEvent);
    }

    public long insertProximityData(SensorEvent sensorEvent) {
        return insertSingleValueData(PROXIMITY_TABLE, sensorEvent);
    }

    public long insertAmbientTemperatureData(SensorEvent sensorEvent) {
        return insertSingleValueData(AMBIENT_TEMPERATURE_TABLE, sensorEvent);
    }

    public long insertRelativeHumidityData(SensorEvent sensorEvent) {
        return insertSingleValueData(RELATIVE_HUMIDITY_TABLE, sensorEvent);
    }

    public long insertStepCounterData(SensorEvent sensorEvent) {
        return insertSingleValueData(STEP_COUNTER_TABLE, sensorEvent);
    }

//    public long insertTemperatureData(SensorEvent sensorEvent) {
//        return insertSingleValueData(TEMPERATURE_TABLE, sensorEvent);
//    }


    // ==================== 辅助方法 ====================
    
    // 🚀 OPTIMIZED: Single connection with batch queue system
    private static class BatchEntry {
        String tableName;
        ContentValues values;
        
        BatchEntry(String tableName, ContentValues values) {
            this.tableName = tableName;
            this.values = values;
        }
    }
    
    private java.util.Queue<BatchEntry> batchQueue = new java.util.LinkedList<>();
    private SQLiteDatabase batchDb = null;
    private static final int BATCH_SIZE = 50; // Commit every 50 inserts total
    private final Object batchLock = new Object(); // Thread safety
    
    private void addToBatch(String tableName, ContentValues values) {
        synchronized (batchLock) {
            batchQueue.offer(new BatchEntry(tableName, values));
            
            if (batchQueue.size() >= BATCH_SIZE) {
                commitBatch();
            }
        }
    }
    
    private void commitBatch() {
        synchronized (batchLock) {
            if (batchQueue.isEmpty()) return;
            
            try {
                if (batchDb == null || !batchDb.isOpen()) {
                    batchDb = getWritableDatabase();
                }
                
                batchDb.beginTransaction();
                
                int processedCount = 0;
                while (!batchQueue.isEmpty()) {
                    BatchEntry entry = batchQueue.poll();
                    batchDb.insert(entry.tableName, null, entry.values);
                    processedCount++;
                }
                
                batchDb.setTransactionSuccessful();
                batchDb.endTransaction();
                
                android.util.Log.d("DatabaseBatch", "✅ Committed " + processedCount + " entries across all tables");
                
            } catch (Exception e) {
                android.util.Log.e("DatabaseBatch", "Error in batch commit: " + e.getMessage());
                if (batchDb != null && batchDb.inTransaction()) {
                    batchDb.endTransaction(); // Rollback
                }
            } finally {
                if (batchDb != null) {
                    batchDb.close();
                    batchDb = null;
                }
            }
        }
    }
    
    // 🔧 Force commit remaining batch data
    public void forceCommitBatch() {
        synchronized (batchLock) {
            if (!batchQueue.isEmpty()) {
                commitBatch();
                android.util.Log.d("DatabaseBatch", "Force committed remaining queue entries");
            }
        }
    }

    public List<String> getAllTableNames() {
        List<String> tables = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(   // 这样的话得到的cursor每一行都是一个table的名字
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'android_metadata' AND name NOT LIKE 'sqlite_sequence'",
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0));
            }
            cursor.close();
        }
        return tables;
    }

    public boolean exportTableToCSV(String filePath) {
//        Log.d("RecordingDatabaseHelper", "=== exportTableToCSV 开始 ===");

        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null || !db.isOpen()) {
//            Log.e("RecordingDatabaseHelper", "数据库未打开或为空");
            return false;
        }

        Cursor cursor = null;
        FileWriter fw = null;
        List<String> tableNames = getAllTableNames();

//        Log.d("RecordingDatabaseHelper", "获取到的表名列表: " + tableNames.toString());
//        Log.d("RecordingDatabaseHelper", "表名数量: " + tableNames.size());

        File dir = new File(filePath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
//            Log.d("RecordingDatabaseHelper", "创建目录结果: " + created);
        }

        for (String tableName : tableNames) {
//            Log.d("RecordingDatabaseHelper", "开始导出表: " + tableName);

            try {
                // 首先检查表中是否有数据
                Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
                int rowCount = 0;
                if (countCursor.moveToFirst()) {
                    rowCount = countCursor.getInt(0);
                }
                countCursor.close();

//                Log.d("RecordingDatabaseHelper", "表 " + tableName + " 的行数: " + rowCount);

                if (rowCount == 0) {
//                    Log.w("RecordingDatabaseHelper", "表 " + tableName + " 为空，跳过导出");
                    continue;
                }

                cursor = db.rawQuery("SELECT * FROM " + tableName, null);
//                Log.d("RecordingDatabaseHelper", "查询结果 cursor 列数: " + cursor.getColumnCount());

                String fileName = filePath + "/" + tableName + ".csv";
//                Log.d("RecordingDatabaseHelper", "创建文件: " + fileName);
                fw = new FileWriter(fileName);

                // Write header
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    fw.append(cursor.getColumnName(i));
                    if (i < cursor.getColumnCount() - 1) {
                        fw.append(",");
                    }
                }
                fw.append("\n");
//                Log.d("RecordingDatabaseHelper", "写入表头完成");

                // Write rows
                int exportedRows = 0;
                while (cursor.moveToNext()) {
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String columnName = cursor.getColumnName(i);

                        // 对经纬度字段保持完整精度
                        if ("lat".equals(columnName) || "lng".equals(columnName) || "acc".equals(columnName)) {
                            double value = cursor.getDouble(i);
                            fw.append(String.format("%.6f", value));
                        } else {
                            String value = cursor.getString(i);
                            fw.append(value != null ? value : "");
                        }

                        if (i < cursor.getColumnCount() - 1) {
                            fw.append(",");
                        }
                    }
                    fw.append("\n");
                    exportedRows++;
                }
                fw.flush();

//                Log.d("RecordingDatabaseHelper", "表 " + tableName + " 导出完成，导出行数: " + exportedRows);

            } catch (Exception e) {
//                Log.e("RecordingDatabaseHelper", "导出表 " + tableName + " 时出错: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                if (cursor != null) cursor.close();
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException ignored) {}
                }
            }
        }

//        Log.d("RecordingDatabaseHelper", "=== exportTableToCSV 完成 ===");
        return true;
    }

    public boolean exportAllLocationKML(String filepath) throws XmlPullParserException, IOException {
        boolean gpsResult = exportSingleLocationKML(filepath, GlobalContants.LOCATION_GPS_TABLE, "GPS轨迹.kml", "GPS Track");
        boolean networkResult = exportSingleLocationKML(filepath, GlobalContants.LOCATION_NETWORK_TABLE, "Network轨迹.kml", "Network Track");
        return gpsResult && networkResult;
    }

    private boolean exportSingleLocationKML(String filepath, String tableName, String fileName, String kmlName) throws XmlPullParserException, IOException {
        StringWriter xmlWriter = new StringWriter();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlSerializer xmlSerializer = factory.newSerializer();
        xmlSerializer.setOutput(xmlWriter);
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        xmlSerializer.startDocument("UTF-8", true);

        xmlSerializer.startTag(null, "kml");
        xmlSerializer.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");
        xmlSerializer.attribute(null, "xmlns:gx", "http://www.google.com/kml/ext/2.2");

        xmlSerializer.startTag(null, "Document");

        xmlSerializer.startTag(null, "name");
        xmlSerializer.text(kmlName + " - " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        xmlSerializer.endTag(null, "name");

        xmlSerializer.startTag(null, "Style");
        xmlSerializer.attribute(null, "id", "trackStyle");
        xmlSerializer.startTag(null, "LineStyle");
        xmlSerializer.startTag(null, "color");
        xmlSerializer.text("ff60ff00");
        xmlSerializer.endTag(null, "color");
        xmlSerializer.startTag(null, "width");
        xmlSerializer.text("6");
        xmlSerializer.endTag(null, "width");
        xmlSerializer.endTag(null, "LineStyle");
        xmlSerializer.endTag(null, "Style");

        xmlSerializer.startTag(null, "Placemark");
        xmlSerializer.startTag(null, "name");
        xmlSerializer.text("GPS Track");
        xmlSerializer.endTag(null, "name");
        xmlSerializer.startTag(null, "styleUrl");
        xmlSerializer.text("#trackStyle");
        xmlSerializer.endTag(null, "styleUrl");

        xmlSerializer.startTag("http://www.google.com/kml/ext/2.2", "Track");

        SQLiteDatabase db = this.getReadableDatabase();

        // 首先我们写一下经纬度
        Cursor cursor = db.rawQuery("SELECT lat, lng FROM " + tableName, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                double lat = cursor.getDouble(0);
                double lng = cursor.getDouble(1);

                xmlSerializer.startTag("http://www.google.com/kml/ext/2.2", "coord");
                xmlSerializer.text(lng + " " + lat + " 0");
                xmlSerializer.endTag("http://www.google.com/kml/ext/2.2", "coord");
            }
        }
        cursor.close();

        // 好了然后让我们来写时间
        cursor = db.rawQuery("SELECT time FROM " + tableName, null);
//        java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
//        isoFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String time = cursor.getString(0);

                xmlSerializer.startTag(null, "when");
                xmlSerializer.text(time);
                xmlSerializer.endTag(null, "when");
            }
        }
        cursor.close();

        xmlSerializer.endTag("http://www.google.com/kml/ext/2.2", "Track");

        xmlSerializer.endTag(null, "Placemark");
        xmlSerializer.endTag(null, "Document");
        xmlSerializer.endTag(null, "kml");
        xmlSerializer.endDocument();

        String xmlStr = xmlWriter.toString();

        File dir = new File(filepath);
        if (!dir.exists() || !dir.isDirectory()) {
            if (!dir.mkdirs()) {
                return false;
            }
        }

        try {
            File file = new File(dir, fileName);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(xmlStr);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
