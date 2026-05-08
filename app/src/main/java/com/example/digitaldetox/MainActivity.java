package com.example.digitaldetox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scrollfirewall.R;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvCurrentUsage;
    private Switch switchTestMode;
    private Button btnEnableService;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateUiRunnable = new Runnable() {
        @Override
        public void run() {
            updateUsageUI();
            handler.postDelayed(this, 1000); // Update every second
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        tvCurrentUsage = findViewById(R.id.tv_current_usage);
        switchTestMode = findViewById(R.id.switch_test_mode);
        btnEnableService = findViewById(R.id.btn_enable_service);

        switchTestMode.setChecked(sessionManager.isTestMode());
        switchTestMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setTestMode(isChecked);
        });

        btnEnableService.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAccessibilityServiceEnabled(this, DigitalDetoxAccessibilityService.class)) {
            btnEnableService.setText("Accessibility Service is ON");
            btnEnableService.setEnabled(false);
        } else {
            btnEnableService.setText(R.string.enable_accessibility);
            btnEnableService.setEnabled(true);
        }
        
        handler.post(updateUiRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateUiRunnable);
    }

    private void updateUsageUI() {
        long accumulated = sessionManager.getAccumulatedTime();
        long limit = sessionManager.getLimit();
        
        long accSec = (accumulated / 1000) % 60;
        long accMin = (accumulated / (1000 * 60)) % 60;
        
        long limSec = (limit / 1000) % 60;
        long limMin = (limit / (1000 * 60)) % 60;
        
        String usageText = String.format("Current Usage: %02d:%02d / %02d:%02d", accMin, accSec, limMin, limSec);
        tvCurrentUsage.setText(usageText);
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + accessibilityService.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            // Error finding setting
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityServiceStr = mStringColonSplitter.next();
                    if (accessibilityServiceStr.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
