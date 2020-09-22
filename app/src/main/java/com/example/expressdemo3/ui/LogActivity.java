package com.example.expressdemo3.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.expressdemo3.R;

import java.util.ArrayList;

public class LogActivity extends AppCompatActivity {

    private LinearLayout myLinearLayout;
    private ArrayList<String> listLog = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        setData();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //启动子线程，获取内容
        new Thread(new RunnablePrintLog()).start();
    }

    private void setData() {
        myLinearLayout = this.findViewById(R.id.myLinearLayout);

        //获取Bundle
        Bundle bundle = getIntent().getExtras();
        listLog = bundle.getStringArrayList("list");
    }

    class RunnablePrintLog implements Runnable {
        int index = 0;

        @Override
        public void run() {
            listLog.forEach(str -> {
                TextView tv = new TextView(LogActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setEnabled(true);
                        tv.setTextIsSelectable(true);
                        tv.setFocusable(true);
                        tv.setLongClickable(true);
                        tv.setText(str);
                        myLinearLayout.addView(tv);
                    }
                });
            });
        }
    }

    public void Click_copy(View view){
        // 获取系统剪贴板
        ClipboardManager mClipboardManager =(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
        ClipData mClipData =ClipData.newPlainText("SDK_LOG", listLog.toString());
        Log.w("aaaa",listLog.toString());
        // 把数据集设置（复制）到剪贴板
        mClipboardManager.setPrimaryClip(mClipData);
    }

}