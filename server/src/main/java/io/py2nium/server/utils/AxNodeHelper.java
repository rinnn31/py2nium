/*
* Apache 2.0 License
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package io.py2nium.server.utils;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.Display;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.Nullable;

import io.py2nium.server.model.AndroidDevice;

public class AxNodeHelper {
    public static Rect getBounds(@Nullable AccessibilityNodeInfo node) {
        int displayId = getNodeDisplayId(node);
        final boolean isDisplayAccessible = AndroidDevice.getInstance().getDisplayById(displayId) != null;
        Rect screen = null;
        if (isDisplayAccessible) {
            Point displaySize = DeviceUtils.getDisplaySize(displayId);
            screen = new Rect(0, 0, displaySize.x, displaySize.y);
        }
        if (node == null) {
            return screen == null ? new Rect() : screen;
        }
        return getVisibleBoundsInScreen(node, screen, false);
    }

    @SuppressLint("CheckResult")
    private static Rect getVisibleBoundsInScreen(AccessibilityNodeInfo node, Rect displayRect, boolean trimScrollableParent) {
        Rect nodeRect = new Rect();
        node.getBoundsInScreen(nodeRect);

        if (displayRect == null) {
            displayRect = new Rect();
        }
        nodeRect.intersect(displayRect);

        Rect bounds = new Rect();
        AccessibilityWindowInfo window = node.getWindow();
        if (window != null) {
            window.getBoundsInScreen(bounds);
            nodeRect.intersect(bounds);
        }

        if (trimScrollableParent) {
            for (AccessibilityNodeInfo ancestor = node.getParent();
                 ancestor != null;
                 ancestor = ancestor.getParent()
            ) {
                if (ancestor.isScrollable()) {
                    Rect ancestorRect = getVisibleBoundsInScreen(
                            ancestor, displayRect, true
                    );
                    nodeRect.intersect(ancestorRect);
                    break;
                }
            }
        }

        return nodeRect;
    }

    public static int getNodeDisplayId(AccessibilityNodeInfo node) {
        if (node != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AccessibilityWindowInfo window = node.getWindow();
            if (window != null) {
                return window.getDisplayId();
            }
        }
        return Display.DEFAULT_DISPLAY;
    }


}
