package com.mobipioneer.lockerkeyboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ihs.commons.config.HSConfig;
import com.ihs.devicemonitor.accessibility.HSAccessibilityService;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.mobipioneer.lockerkeyboard.app.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isAccessibilityEnabled = HSConfig.optBoolean(false, "Application", "AutoSetKeyEnable");
        Intent intent = getIntent();//携带其他页面的数据
        if (intent == null) {
            intent = new Intent();
        }

        intent.setClass(this, MainActivity.class);
        if (isAccessibilityEnabled) {
            if (!HSAccessibilityService.isAvailable()) {
                intent.setClass(this, KeyboardActivationActivity.class);
            } else if(!HSInputMethod.isCurrentIMESelected()){
                intent.setClass(this, KeyboardWakeUpActivity.class);
            }
        }
        startActivity(intent);
        finish();

    }

}