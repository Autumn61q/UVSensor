package com.example.uvsensor.adapter;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uvsensor.DetailFragment;
import com.example.uvsensor.R;
import com.example.uvsensor.bean.ConfigItem;
import com.example.uvsensor.bean.LocationType;
import com.example.uvsensor.bean.SensorType;
import com.example.uvsensor.listener.ConfigChangedListener;
import com.example.uvsensor.listener.ToDetailFragmentListener;

import java.util.List;

public class ConfigAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private SensorManager sensorManager;
//    private List<SensorType> sensorTypeList;
    private List<ConfigItem> configItemList;
    private LayoutInflater layoutInflater;
    private Context context;
    private ConfigChangedListener configChangedListener;
    private ToDetailFragmentListener toDetailFragmentListener;

    public ConfigAdapter(List<ConfigItem> configItemList, Context context) {
//        this.sensorTypeList = sensorTypeList;
        this.configItemList = configItemList;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
//        sensorManager = (SensorManager) this.context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {
        return configItemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) { // 传感器
            View view = layoutInflater.inflate(R.layout.sensor_item_layout, parent, false);
            return new SensorViewHolder(view);
        } else if (viewType == 1) { // 定位方式
            View view = layoutInflater.inflate(R.layout.sensor_item_layout, parent, false);
            return new LocationViewHolder(view);
        } else {  // else if (viewType == 2)
            // 分界线
            View view = layoutInflater.inflate(R.layout.divider_item_layout, parent, false);
            return new DividerViewHolder(view);
        }
    }

    // 它会在 RecyclerView 需要显示某个 item 时被调用, 某个item只要由不可见变为可见，onBindViewHolder就会为它的position调用一次
    // 这样是为了高效复用内存和控件，只维护屏幕上可见的少量 ViewHolder，避免一次性创建所有 item 的视图，节省资源
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {  // 这里的holder就是下面我们自己写的两个ViewHolder
        ConfigItem item = configItemList.get(position);
        if (holder instanceof SensorViewHolder) {

            SensorViewHolder sensorViewHolder = (SensorViewHolder) holder;
            SensorType sensorType = (SensorType) configItemList.get(position);
            sensorViewHolder.sensor_name.setText(item.getItem_name());  // 把对应的传感器数据显示在列表上

            // 如果传感器在设备中存在，那就可以自由操作；如果不存在，那就disable它对应的开关
            if (sensorType.isExist()) {
                // 首先，上次是开就是开，上次是关就是关
                if (sensorType.isChecked()) {
                    sensorViewHolder.sensor_switch.setChecked(true);
                } else {
                    sensorViewHolder.sensor_switch.setChecked(false);
                }
                // 其次，设置点击的监听事件
                sensorViewHolder.sensor_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            // holder.sensor_switch.setChecked(true);  这行代码并不需要！并不需要我们手动设置开关的状态
                            sensorType.setChecked(true);
                            configChangedListener.addSensor(sensorType.getSensor_type());
                        } else {
                            sensorType.setChecked(false);
                            configChangedListener.deleteSensor(sensorType.getSensor_type());
                        }
                    }
                });
            } else {
//                sensorViewHolder.sensor_switch.setEnabled(false);  // 无法打开
                sensorViewHolder.sensor_switch.setChecked(false);  // 关闭开关
                sensorViewHolder.sensor_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked) {
                            Toast.makeText(context, "该设备不含" + sensorType.getItem_name(), Toast.LENGTH_SHORT).show();
                            compoundButton.setChecked(false);
                        }
                    }
                });
            }

            sensorViewHolder.iv_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (toDetailFragmentListener != null) {
                        toDetailFragmentListener.onDetailClick(sensorType);
                    }
                }
            });
        }
        else if (holder instanceof LocationViewHolder) {

            LocationViewHolder locationViewHolder = (LocationViewHolder) holder;
            LocationType locationType = (LocationType) item;
            locationViewHolder.location_name.setText(locationType.getItem_name());

            locationViewHolder.location_switch.setChecked(locationType.isChecked());

            locationViewHolder.location_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        locationType.setChecked(true);
                        configChangedListener.addLocationType(locationType.getLocation_type());
                    } else {
                        locationType.setChecked(false);
                        configChangedListener.deleteLocationType(locationType.getLocation_type());
                    }
                }
            });
            locationViewHolder.iv_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (toDetailFragmentListener != null) {
                        toDetailFragmentListener.onDetailClick(locationType);
                    }
                }
            });
        }
        else if (holder instanceof DividerViewHolder) {
            return;
        }
    }

    @Override
    public int getItemCount() {
        return configItemList.size();
    }

    // 这个函数我觉得不用在adapter初始化的时候调用也可以，因为它会在configFragment初始化的时候调用，应该有差不多相同的效果
    public void setConfigChangedListener(ConfigChangedListener configChangedListener) {
            this.configChangedListener = configChangedListener;
    }

    // 和setConfigChangedListener一样，也是configfragment和adapter的接口函数
    public void setToDetailFragmentListener(ToDetailFragmentListener toDetailFragmentListener) {
        this.toDetailFragmentListener = toDetailFragmentListener;
    }

    public class SensorViewHolder extends RecyclerView.ViewHolder{
        TextView sensor_name;
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch sensor_switch;
        ImageView iv_detail;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            this.sensor_name = itemView.findViewById(R.id.tv_item_name);
            this.sensor_switch = itemView.findViewById(R.id.item_switch);
            this.iv_detail = itemView.findViewById(R.id.detail);
        }
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView location_name;
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch location_switch;
        ImageView iv_detail;
        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            this.location_name = itemView.findViewById(R.id.tv_item_name);
            this.location_switch = itemView.findViewById(R.id.item_switch);
            this.iv_detail = itemView.findViewById(R.id.detail);
        }
    }

    public static class DividerViewHolder extends RecyclerView.ViewHolder {
        public DividerViewHolder(@NonNull View itemView) { super(itemView); }
    }
}
