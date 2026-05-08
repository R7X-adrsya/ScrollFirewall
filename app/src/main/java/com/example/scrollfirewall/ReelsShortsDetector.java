package com.example.scrollfirewall;

import android.view.accessibility.AccessibilityNodeInfo;

public class ReelsShortsDetector {

    /**
     * Detects if the current screen is Instagram Reels.
     */
    public boolean isInstagramReels(AccessibilityNodeInfo root) {
        if (root == null) return false;

        // Detect Instagram Reels player containers based on known view IDs
        return !root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_video_container").isEmpty() ||
               !root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/reel_viewer_root").isEmpty() ||
               !root.findAccessibilityNodeInfosByViewId("com.instagram.android:id/reels_video_container").isEmpty();
    }
}
