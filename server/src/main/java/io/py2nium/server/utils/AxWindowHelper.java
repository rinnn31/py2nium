package io.py2nium.server.utils;

import android.os.Build;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.test.uiautomator.UiDevice;

import java.util.ArrayList;
import java.util.List;

import io.py2nium.server.core.UiAutomatorWrapper;

public class AxWindowHelper {
    private static final long AX_ROOT_RETRIEVAL_TIMEOUT_MS = 10000;
    private static final long AX_ROOT_RETRIEVAL_INTERVAL_MS = 250;

    public static List<AccessibilityWindowInfo> getWindows() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final List<AccessibilityWindowInfo> windowList = new ArrayList<>();
            final SparseArray<List<AccessibilityWindowInfo>> allWindows = UiAutomatorWrapper.getUiAutomation().getWindowsOnAllDisplays();
            for (int index = 0; index < allWindows.size(); index++) {
                windowList.addAll(allWindows.valueAt(index));
            }
            return windowList;
        }
        return UiAutomatorWrapper.getUiAutomation().getWindows();
    }

    public static List<AccessibilityNodeInfo> getWindowRoots() {
        List<AccessibilityNodeInfo> roots = new ArrayList<>();

        AccessibilityNodeInfo root = UiAutomatorWrapper.getUiAutomation().getRootInActiveWindow();
        if(root != null) {
            roots.add(root);
        }

        List<AccessibilityWindowInfo> windows = getWindows();
        for(int i = 0 ;i< windows.size();i++) {
            AccessibilityNodeInfo windowRoot = windows.get(i).getRoot();
            if(windowRoot != null && (root == null || windowRoot != root)) {
                roots.add(windowRoot);
            }
        }

        return roots;
    }

    public static AccessibilityNodeInfo getActiveWindowRoot() {
        long startTime = System.currentTimeMillis();
        AccessibilityNodeInfo root = null;
        while (System.currentTimeMillis() - startTime < AX_ROOT_RETRIEVAL_TIMEOUT_MS) {
            root = UiAutomatorWrapper.getUiAutomation().getRootInActiveWindow();
            if (root != null) {
                break;
            }

            SystemClock.sleep(AX_ROOT_RETRIEVAL_INTERVAL_MS);
        }
        return root;
    }
}
