package com.example.expressdemo3.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.expressdemo3.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setOnclickEvent();
        /** 申请权限 */
        checkOrRequestPermission();
    }

    /**
     * 设置菜单栏menu布局
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 监听菜单栏，设置按钮
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                this.startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOnclickEvent() {
        findViewById(R.id.btn_1).setOnClickListener((view) -> {
            Intent intent = new Intent(MainActivity.this, BaseActivity.class);
            this.startActivity(intent);
        });
        findViewById(R.id.btn_2).setOnClickListener((view) -> {
            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
            this.startActivity(intent);
        });
        findViewById(R.id.btn_3).setOnClickListener((view) -> {
            Intent intent = new Intent(MainActivity.this, PublishActivity.class);
            this.startActivity(intent);
        });
        findViewById(R.id.btn_4).setOnClickListener((view) -> {
            Intent intent = new Intent(MainActivity.this, MediaPlayerActivity.class);
            this.startActivity(intent);
        });
        findViewById(R.id.btn_5).setOnClickListener((view) -> {
            Intent intent = new Intent(MainActivity.this, WhiteboardActivity.class);
            this.startActivity(intent);
        });
    }

    /**
     * 校验并请求权限，动态授权
     */
    public boolean checkOrRequestPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(PERMISSIONS_STORAGE, 101);
                return false;
            }
        }
        return true;
    }

}