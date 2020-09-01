package com.example.expressdemo3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class LogActivity extends AppCompatActivity {

    private LinearLayout myLinearLayout;
    private ArrayList<String> listLog = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        myLinearLayout = this.findViewById(R.id.myLinearLayout);

        //获取Bundle
        Bundle bundle = getIntent().getExtras();
        listLog = bundle.getStringArrayList("list");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //启动子线程，获取内容
        new Thread(new RunnablePrintLog()).start();
    }

    class RunnablePrintLog implements Runnable {
        int index =0;
        @Override
        public void run() {
            listLog.forEach(str -> {
                TextView textview = new TextView(LogActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText(str);
                        myLinearLayout.addView(textview);
                    }
                });
            });
        }
    }

}