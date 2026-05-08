package com.example.scrollfirewall;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ScrollFirewallAccessibilityService extends AccessibilityService {

    private static final String TAG = "ScrollFirewallService";
    private static final String PKG_INSTAGRAM = "com.instagram.android";

    private SessionManager sessionManager;
    private ReelsShortsDetector detector;
    private long lastCheck = 0;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        sessionManager = new SessionManager(this);
        detector = new ReelsShortsDetector();
        Log.d(TAG, "Service connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();

        boolean isInstagram = pkg.equals(PKG_INSTAGRAM);

        if (!isInstagram) {
            sessionManager.pauseAndReset();
            return;
        }

        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }

        // Performance Fix: Debounce
        long now = System.currentTimeMillis();
        if (now - lastCheck < 800) return;
        lastCheck = now;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        boolean isReels = detector.isInstagramReels(root);

        // Core Logic: ONLY handle Instagram Reels
        if (isReels) {
            sessionManager.startOrResume();

            if (sessionManager.isLimitReached()) {
                Log.d(TAG, "Limit reached while in Reels. Blocking.");
                performGlobalAction(GLOBAL_ACTION_HOME);
                launchBlockingActivity();
            }
        } else {
            sessionManager.pauseAndReset();
        }

        root.recycle();
    }

    private void launchBlockingActivity() {
        Intent intent = new Intent(this, BlockingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {
        if (sessionManager != null) {
            sessionManager.pauseAndReset();
        }
    }
}
