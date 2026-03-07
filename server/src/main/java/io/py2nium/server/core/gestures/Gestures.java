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


package io.py2nium.server.core.gestures;

import static io.py2nium.server.utils.ReflectionUtils.getField;
import static io.py2nium.server.utils.ReflectionUtils.getMethod;
import static io.py2nium.server.utils.ReflectionUtils.invokeMethod;

import android.graphics.Point;
import android.graphics.Rect;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.py2nium.server.utils.ReflectionUtils;

public class Gestures {
    private final Class<?> wrappedClass;
    private final int displayId;

    Gestures(int displayId) {
        this.displayId = displayId;
        this.wrappedClass = ReflectionUtils.getClass("androidx.test.uiautomator.Gestures");
    }

    public int getDisplayId() {
        return displayId;
    }

    public PointerGesture drag(Point start, Point end, int speed) {
        Method dragMethod = getMethod(
                wrappedClass, "drag",
                Point.class, Point.class, int.class, int.class
        );
        return new PointerGesture(invokeMethod(dragMethod, wrappedClass, start, end, speed, displayId));
    }

    public PointerGesture swipe(Point start, Point end, int speed) {
        Method swipeMethod = getMethod(
                wrappedClass, "swipe",
                Point.class, Point.class, int.class, int.class
        );
        return new PointerGesture(invokeMethod(swipeMethod, wrappedClass, start, end, speed, displayId));
    }

    public PointerGesture swipe(Rect area, Direction direction, float percent, int speed) {
        Method swipeRectMethod = getMethod(
                wrappedClass, "swipeRect",
                Rect.class, Direction.class, float.class, int.class, int.class
        );
        return new PointerGesture(invokeMethod(swipeRectMethod, wrappedClass, area, direction, percent, speed, displayId));
    }


    public PointerGesture[] pinchClose(Rect area, float percent, int speed) {
        Method pinchCloseMethod = getMethod(
                wrappedClass, "pinchClose",
                Rect.class, float.class, int.class, int.class
        );
        return toGesturesArray(
                invokeMethod(pinchCloseMethod, wrappedClass, area, percent, speed, displayId)
        );
    }

    public PointerGesture[] pinchOpen(Rect area, float percent, int speed) {
        Method pinchOpenMethod = getMethod(
                wrappedClass, "pinchOpen",
                Rect.class, float.class, int.class, int.class
        );
        return toGesturesArray(
                invokeMethod(pinchOpenMethod, wrappedClass, area, percent, speed, displayId)
        );
    }



    private PointerGesture[] toGesturesArray(Object result) {
        List<PointerGesture> list = new ArrayList<>();
        for (int i = 0; i < Array.getLength(result); ++i) {
            list.add(new PointerGesture(Array.get(result, i)));
        }
        return list.toArray(new PointerGesture[0]);
    }

    public static float getDisplayDensity() {
        return InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getResources().getDisplayMetrics().density;
    }

    private static int getSpeedValue(String gestureName) {
        String fieldName = String.format("DEFAULT_%s_SPEED", gestureName.toUpperCase());
        return (int) getField(UiObject2.class, fieldName);
    }

    public static int getDefaultDragSpeed() {
        return (int) (getSpeedValue("drag") * getDisplayDensity());
    }

    public static int getDefaultSwipeSpeed() {
        return (int) (getSpeedValue("swipe") * getDisplayDensity());
    }

    public static int getDefaultScrollSpeed() {
        return (int) (getSpeedValue("scroll") * getDisplayDensity());
    }

    public static int getDefaultFlingSpeed() {
        return (int) (getSpeedValue("fling") * getDisplayDensity());
    }

    public static int getDefaultPinchSpeed() {
        return (int) (getSpeedValue("pinch") * getDisplayDensity());
    }

    public static long getScrollTimeout() {
        // UiObject2.SCROLL_TIMEOUT
        return 1000L;
    }

    public static long getFlingTimeout() {
        // UiObject2.FLING_TIMEOUT
        return 5000L;
    }
}
