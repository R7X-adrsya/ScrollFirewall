package com.example.scrollfirewall;

import android.os.Bundle;
import android.widget.Button;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class BlockingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocking);
        
        Button btnClose = findViewById(R.id.btn_close_blocking);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // Modern OnBackPressedDispatcher replacement for onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }
}
