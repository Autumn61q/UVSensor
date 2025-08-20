package com.example.uvsensor;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    public HomeFragment homeFragment;
    public ConfigFragment configFragment;
    private MeFragment meFragment;
    private DetailFragment detailFragment;
    private Bundle detailFragmentBundle;
    private OnBackPressedCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.System.canWrite(this)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                intent.setData(Uri.parse("package:" + getPackageName()));
//                startActivity(intent);
//            }
//        }

        // 改一下状态栏（就是电量、时间那里）的颜色（不然显示紫色）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.deep_green));
        }

        Log.d("MainActivity", "onCreating is called");

        detailFragmentBundle = new Bundle();

        initBottomNavigation();

        // 默认在config这个fragment
        selectedFragment(1);

    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    private void initBottomNavigation(){
        bottomNavigationView = findViewById(R.id.navigation_bar_item_icon_container);
        // Set config as selected by default
        bottomNavigationView.setSelectedItemId(R.id.config);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 回到主页面（pop掉 detail 等非主页面）
                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                // 再 show 对应主页面 Fragment
                if (item.getItemId() == R.id.home) {
                    selectedFragment(0);
                } else if (item.getItemId() == R.id.config) {
                    if (detailFragment == null) {  // 如果detail没有展示的话，我们就正常显示config
                        selectedFragment(1);
                    } else {  // 就是说detailFragment已经被实例化，那我们切换到selectedFragment
                        selectedFragment((3));
                    }
                } else if (item.getItemId() == R.id.me) {
                    selectedFragment(2);
                }
                return true;
            }
        });

//        callback = new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//
//                if (homeFragment != null) {
//                    homeFragment.finishRecording(false);
//                }
//                showExitConfirmationDialog();
//            }
//        };
//
//        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    private void selectedFragment(int position) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();  // 一个fragment管理器
        hideFragment(fragmentTransaction);

        if (position == 0){
            if (homeFragment == null){
                homeFragment = new HomeFragment();
                fragmentTransaction.add(R.id.content, homeFragment);
            }
            else {
                fragmentTransaction.show(homeFragment);  // show的话fragment不会有任何生命周期的调用
            }
        }
        else if (position == 1){
            if (configFragment == null){
                configFragment = new ConfigFragment();
                fragmentTransaction.add(R.id.content, configFragment);
            }
            else {
                fragmentTransaction.show(configFragment);
            }
        }
        else if (position == 2){
            if (meFragment == null){
                meFragment = new MeFragment();
                fragmentTransaction.add(R.id.content, meFragment);
            }
            else {
                fragmentTransaction.show(meFragment);
            }
        }
        else if (position == 3) {
            if (detailFragment == null) {
                if (detailFragmentBundle != null) {
                    detailFragment = new DetailFragment();
                    detailFragment.setArguments(detailFragmentBundle);
                    fragmentTransaction.add(R.id.content, detailFragment);
                }
            }
            else {
                fragmentTransaction.show(detailFragment);
            }
        }

        // 记得提交一下
        fragmentTransaction.commit();

    }

    private void hideFragment(FragmentTransaction fragmentTransaction){
        if (homeFragment != null){
            fragmentTransaction.hide(homeFragment);  // hide的话fragment不会有任何生命周期的调用
        }
        if (configFragment != null){
            fragmentTransaction.hide(configFragment);
        }
        if (meFragment != null){
            fragmentTransaction.hide(meFragment);
        }
        if (detailFragment != null) {
            fragmentTransaction.hide(detailFragment);
        }
    }

    // 接口，用来被config调用，创建detailFragment
    public void setDetailFragmentBundle(Bundle bundle) {
        this.detailFragmentBundle = bundle;
        selectedFragment(3);
    }

    // 接口，用来被config调用，销毁detailFragment
    public void destoryDetailFragment() {
        if (detailFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(detailFragment);
            transaction.commit();
            detailFragment = null;

            selectedFragment(1);
        }
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确定退出应用吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户确认退出
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户取消退出
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}