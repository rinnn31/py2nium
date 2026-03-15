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


package io.py2nium.server.model;

import static io.py2nium.server.utils.StringUtils.charSequenceToString;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.Nullable;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import java.util.Map;
import java.util.UUID;

import io.py2nium.server.core.gestures.GestureController;
import io.py2nium.server.utils.Attribute;
import io.py2nium.server.utils.AxNodeHelper;
import io.py2nium.server.utils.ReflectionUtils;


public class AndroidElement {
    public static final Attribute[] EXPOSED_ATTRIBUTES = new Attribute[] {
            Attribute.INDEX, Attribute.CLASS, Attribute.PACKAGE, Attribute.RESOURCE_ID,
            Attribute.CONTENT_DESC, Attribute.TEXT, Attribute.HINT, Attribute.PASSWORD,
            Attribute.CHECKABLE, Attribute.CLICKABLE, Attribute.LONG_CLICKABLE, Attribute.ENABLED,
            Attribute.CHECKED, Attribute.DISPLAYED, Attribute.FOCUSABLE, Attribute.FOCUSED,
            Attribute.SCROLLABLE, Attribute.SELECTED, Attribute.BOUNDS
    };


    private final AccessibilityNodeInfo mNodeRoot;
    private final String uuid = UUID.randomUUID().toString();
    private int mIndex = 0;
    private final int mDisplayId;


    public AndroidElement(AccessibilityNodeInfo nodeRoot, int index) {
        this.mNodeRoot = nodeRoot;
        this.mIndex = index;

        this.mDisplayId = AxNodeHelper.getNodeDisplayId(mNodeRoot);
    }

    public static AndroidElement obtain(AndroidElement element) {
        return new AndroidElement(AccessibilityNodeInfo.obtain(element.mNodeRoot), element.mIndex);
    }

    public Object getAttributeValue(Attribute attribute) {
        switch (attribute) {
            case CLASS:
                return charSequenceToString(mNodeRoot.getClassName());
            case PACKAGE:
                return charSequenceToString(mNodeRoot.getPackageName());
            case RESOURCE_ID:
                return mNodeRoot.getViewIdResourceName();
            case TEXT:
                return charSequenceToString(mNodeRoot.getText());
            case HINT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return charSequenceToString(mNodeRoot.getHintText());
                } else return "";
            case CONTENT_DESC:
                return charSequenceToString(mNodeRoot.getContentDescription());
            case PASSWORD:
                return mNodeRoot.isPassword();
            case CLICKABLE:
                return mNodeRoot.isClickable();
            case LONG_CLICKABLE:
                return mNodeRoot.isLongClickable();
            case FOCUSABLE:
                return mNodeRoot.isFocusable();
            case FOCUSED:
                return mNodeRoot.isFocused();
            case INDEX:
                return mIndex;
            case DISPLAYED:
                return mNodeRoot.isVisibleToUser();
            case BOUNDS:
                return AxNodeHelper.getBounds(mNodeRoot).toShortString();
            case SELECTED:
                return mNodeRoot.isSelected();
            case SCROLLABLE:
                return mNodeRoot.isScrollable();
            case CHECKABLE:
                return mNodeRoot.isCheckable();
            case CHECKED:
                return mNodeRoot.isChecked();
            case ENABLED:
                return mNodeRoot.isEnabled();
            default:
                return "";
        }
    }

    public Object getAttributeValue(String attributeStr) {
        Attribute attribute = Attribute.fromString(attributeStr);
        if(attribute == null) return "";
        return getAttributeValue(attribute);
    }

    public boolean performAction(int action, Map<String, Object> args) {
        Bundle bundle = new Bundle();
        for(Map.Entry<String, Object> entry : args.entrySet()) {
            if(entry.getValue() instanceof String) {
                bundle.putCharSequence(entry.getKey(), (CharSequence) entry.getValue());
            } else if(entry.getValue() instanceof Integer) {
                bundle.putInt(entry.getKey(), (Integer) entry.getValue());
            } else if(entry.getValue() instanceof Boolean) {
                bundle.putBoolean(entry.getKey(), (Boolean) entry.getValue());
            } else {
                throw new IllegalArgumentException("Unsupported type for action argument. Only String, Integer and Boolean are supported.");
            }
        }

        return mNodeRoot.performAction(action, bundle);
    }

    public AccessibilityNodeInfo getNode() {
        return mNodeRoot;
    }

    public @Nullable AndroidElement getParent() {
        AccessibilityNodeInfo parent = mNodeRoot.getParent();
        if(parent == null) return null;
        return new AndroidElement(parent, 0);
    }

    public ElementCollection getChildren() {
        ElementCollection elements = new ElementCollection();

        for(int i = 0;i<mNodeRoot.getChildCount();i++) {
            AccessibilityNodeInfo child = mNodeRoot.getChild(i);
            if(child == null) continue;
            elements.add(new AndroidElement(child, i));
        }
        return elements;
    }

    public AndroidElement getChild(int index) {
        if(index >= mNodeRoot.getChildCount()) return null;
        return new AndroidElement(mNodeRoot.getChild(index), index);
    }

    public int getChildCount() {
        return mNodeRoot.getChildCount();
    }

    public boolean refresh() {
        return this.mNodeRoot.refresh();
    }

    public String getUUID() {
        return uuid;
    }

    public int getDisplayId() {
        return mDisplayId;
    }

    public void click(@Nullable Integer xOffset, @Nullable Integer yOffset, @Nullable Long duration) {
        GestureController controller = AndroidDevice.getInstance().getGestureController(mDisplayId);

        if(duration == null)
            controller.click(getValidPointInBound(xOffset, yOffset));
        else
            controller.longClick(getValidPointInBound(xOffset, yOffset),duration);
    }

    public void doubleClick(@Nullable Integer xOffset, @Nullable Integer yOffset) {
        GestureController controller = AndroidDevice.getInstance().getGestureController(mDisplayId);
        controller.doubleClick(getValidPointInBound(xOffset, yOffset));
    }

    public boolean fling(Direction direction, @Nullable Integer speed ) {
        Rect bound = AxNodeHelper.getBounds(mNodeRoot);
        GestureController controller = AndroidDevice.getInstance().getGestureController(mDisplayId);

        return controller.fling(bound, direction, speed);
    }

    public void drag(Point dest, @Nullable Integer speed) {
        Rect bound = AxNodeHelper.getBounds(mNodeRoot);
        GestureController controller = AndroidDevice.getInstance().getGestureController(mDisplayId);
        Point start = new Point(bound.centerX(), bound.centerY());

        controller.drag(start, dest, speed);
    }

    public void swipe(Direction direction, float percent, @Nullable Integer speed) {
        Rect bound = getVisibleBoundsForGestures();
        GestureController controller = AndroidDevice.getInstance().getGestureController(mDisplayId);

        controller.swipeRect(bound, direction, percent, speed);
    }


    public void pinchOpen(float percent, @Nullable Integer speed) {
        Rect bound = getVisibleBoundsForGestures();
        GestureController controller = AndroidDevice.getInstance().getGestureController(mDisplayId);

        controller.pinchOpen(bound, percent, speed);
    }

    public void pinchClose(float percent, @Nullable Integer speed) {
        Rect bound = getVisibleBoundsForGestures();
        GestureController controller = AndroidDevice.getInstance().getGestureController(mDisplayId);

        controller.pinchClose(bound, percent, speed);
    }

    public void setText(String text, boolean isAppendMode) {
        if(text == null) text = "";
        if(isAppendMode) {
            CharSequence currentText = mNodeRoot.getText();
            if(currentText != null) text = currentText + text;
        }

        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        mNodeRoot.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    private Point getValidPointInBound(Integer xOffset, Integer yOffset) {
        Rect bound = AxNodeHelper.getBounds(mNodeRoot);

        int finalX = (xOffset == null ? 0 : xOffset) + bound.centerX();
        if(finalX < bound.left) finalX = bound.left;
        if(finalX > bound.right) finalX = bound.right;

        int finalY = (yOffset == null ?  0 : yOffset) + bound.centerY();
        if(finalY < bound.top) finalY = bound.top;
        if(finalY > bound.bottom) finalY = bound.bottom;

        return new Point(finalX, finalY);
    }

    public Point getCenter() {
        Rect bound = AxNodeHelper.getBounds(mNodeRoot);
        return new Point(bound.centerX(), bound.centerY());
    }

    public Rect getVisibleBoundsForGestures() {
        Rect bounds = AxNodeHelper.getBounds(mNodeRoot);
        float percentageMargin = (float) ReflectionUtils.getField(UiObject2.class, "DEFAULT_GESTURE_MARGIN_PERCENT");
        return new Rect(bounds.left + (int) (bounds.width() * percentageMargin),
                bounds.top + (int) (bounds.height() * percentageMargin),
                bounds.right - (int) (bounds.width() * percentageMargin),
                bounds.bottom - (int) (bounds.height() * percentageMargin));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AndroidElement) {
            AndroidElement element = (AndroidElement) obj;
            return element.mNodeRoot.equals(mNodeRoot);
        }
        return false;
    }

}
