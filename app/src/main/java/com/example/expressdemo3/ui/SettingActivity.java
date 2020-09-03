package com.example.expressdemo3.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.expressdemo3.AppConfig;
import com.example.expressdemo3.R;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

public class SettingActivity extends AppCompatActivity {
    private static int REQUEST_CODE = 666;

    private EditText ed_appID;
    private EditText ed_appSign;
    private EditText ed_initDomain;
    private EditText ed_secret;
    private Spinner sp_testEnv;
    private Spinner sp_advanced;
    private CheckBox cb_play_ultra;
    private CheckBox cb_login_auth;
//    private CheckBox cb_publish_auth;
//    private CheckBox cb_play_auth;

    private Button btn_scan;

    private Long appID;
    private String appSign;
    private boolean isTestEnv;
    private boolean isOpenAdvanced;
    private String initDomain;
    private String serverSecret;
    private boolean isPlayUltra;
    private boolean isLoginAuth;
    private boolean isPublishAuth;
    private boolean isPlayAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_setting);

        /** 二维码扫描库 */
        ZXingLibrary.initDisplayOpinion(SettingActivity.this);

        setView();//获取view 并设置监听
        setViewData();//设置view Data
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        setAppConfig();//设置AppConfig类的配置信息
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** 处理二维码扫描结果 */
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    try {
                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);

                        ed_appID.setText(jsonObject.getLong("appID").toString());
                        ed_appSign.setText(jsonObject.getString("appSign"));
                        if (jsonObject.getBoolean("isTestEnv"))
                            sp_testEnv.setSelection(0, true);
                        else
                            sp_testEnv.setSelection(1, true);
                        if (jsonObject.getBoolean("isOpenAdvanced")) {
                            sp_advanced.setSelection(1, true);
                            ed_initDomain.setText(jsonObject.getString("initDomain"));
                            cb_play_ultra.setChecked(jsonObject.getBoolean("isPlayUltra"));
                        } else {
                            sp_advanced.setSelection(0, true);
                            ed_initDomain.setText("");
                            cb_play_ultra.setChecked(false);
                        }
                        ed_secret.setText(jsonObject.getString("secret"));
                        cb_login_auth.setChecked(jsonObject.getBoolean("isLoginAuth"));
//                        cb_publish_auth.setChecked(jsonObject.getBoolean("isPublishAuth"));
//                        cb_play_auth.setChecked(jsonObject.getBoolean("isPlayAuth"));

                        Toast.makeText(SettingActivity.this, "Scan the QR code successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("ExpressDemo", "JSON parse failure : " + e.toString());
                        Toast.makeText(SettingActivity.this, "JSON parse failure", Toast.LENGTH_SHORT).show();
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(SettingActivity.this, "Scan the QR code failure", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void setView() {
        ed_appID = findViewById(R.id.ed_appID);
        ed_appSign = findViewById(R.id.ed_appSign);
        ed_initDomain = findViewById(R.id.ed_initDomain);
        sp_testEnv = findViewById(R.id.sp_testEnv);
        sp_advanced = findViewById(R.id.sp_advanced);
        cb_play_ultra = findViewById(R.id.cb_play_ultra);
        cb_login_auth = findViewById(R.id.cb_login_auth);
//        cb_publish_auth = findViewById(R.id.cb_publish_auth);
//        cb_play_auth = findViewById(R.id.cb_play_auth);
        btn_scan = findViewById(R.id.btn_scan);
        ed_secret = findViewById(R.id.ed_secret);

        sp_testEnv.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        isTestEnv = true;
                        break;
                    case 1:
                        isTestEnv = false;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        sp_advanced.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        isOpenAdvanced = false;
                        break;
                    case 1:
                        isOpenAdvanced = true;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        btn_scan.setOnClickListener(view -> {
            Intent intent = new Intent(SettingActivity.this, CaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        });
    }

    private void setViewData() {
        AppConfig appConfig = AppConfig.getInstance();
        ed_appID.setText(appConfig.getAppID().toString());
        ed_appSign.setText(appConfig.getAppSign());
        if (appConfig.isTestEnv())
            sp_testEnv.setSelection(0, true);
        else
            sp_testEnv.setSelection(1, true);
        if (appConfig.isOpenAdvanced()) {
            sp_advanced.setSelection(1, true);
            ed_initDomain.setText(appConfig.getInitDomain());
            cb_play_ultra.setChecked(appConfig.isPlayUltra());
        } else {
            sp_advanced.setSelection(0, true);
            ed_initDomain.setText("");
            cb_play_ultra.setChecked(false);
        }
        ed_secret.setText(appConfig.getServerSecret());
        cb_login_auth.setChecked(appConfig.isLoginAuth());
//        cb_publish_auth.setChecked(appConfig.isPublishAuth());
//        cb_play_auth.setChecked(appConfig.isPlayAuth());
    }

    private void getEditText() {
        if (!ed_appID.getText().toString().equals(""))
            appID = Long.parseLong(ed_appID.getText().toString());
        appSign = ed_appSign.getText().toString();
        initDomain = ed_initDomain.getText().toString();
        serverSecret = ed_secret.getText().toString();
    }

    private void getChecked() {
        if (cb_play_ultra.isChecked())
            isPlayUltra = true;
        if (cb_login_auth.isChecked())
            isLoginAuth = true;
//        if (cb_publish_auth.isChecked())
//            isPublishAuth = true;
//        if (cb_play_auth.isChecked())
//            isPlayAuth = true;
    }

    private void setAppConfig() {
        AppConfig appConfig = AppConfig.getInstance();
        getChecked();
        getEditText();

        appConfig.setAppID(appID);
        appConfig.setAppSign(appSign);
        appConfig.setTestEnv(isTestEnv);
        appConfig.setOpenAdvanced(isOpenAdvanced);
        if (isOpenAdvanced) {
            appConfig.setInitDomain(initDomain);
            appConfig.setPlayUltra(isPlayUltra);
        } else {
            appConfig.setInitDomain("");
            appConfig.setPlayUltra(false);
        }
        appConfig.setServerSecret(serverSecret);
        appConfig.setLoginAuth(isLoginAuth);
        appConfig.setPublishAuth(isPublishAuth);
        appConfig.setPlayAuth(isPlayAuth);

        Log.w("ExpressDemo", appConfig.toString());
    }

}