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


import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.EventCondition;
import androidx.test.uiautomator.Until;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;

import io.py2nium.server.core.UiAutomatorWrapper;
import io.py2nium.server.model.AndroidDevice;
import io.py2nium.server.utils.ReflectionUtils;

public class GestureController {
    private final static long DEFAULT_DOUBLE_CLICK_TIMEOUT = 300L;
    private final static long DEFAULT_LONG_CLICK_TIMEOUT = 1000L;
    private final Object wrappedInstance;
    private final Method performGestureMethod;
    private final Gestures gestures;

    public GestureController(Object wrappedInstance, int displayId) {
        this.wrappedInstance = wrappedInstance;
        this.performGestureMethod = extractPerformGestureMethod(wrappedInstance);
        this.gestures = new Gestures(displayId);
    }

    public GestureController(Object wrappedInstance) {
        this(wrappedInstance, AndroidDevice.getInstance().getTopmostWindowDisplayId());
    }

    private static Method extractPerformGestureMethod(Object wrappedInstance) {
        for (Method method : wrappedInstance.getClass().getDeclaredMethods()) {
            if (method.getName().equals("performGesture")) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException(String.format("Cannot retrieve performGesture method from %s",
                wrappedInstance.getClass().getCanonicalName()));
    }

    private void performGesture(PointerGesture... gestures) {
        Object args = Array.newInstance(PointerGesture.getWrappedClass(), gestures.length);
        for (int i = 0; i < gestures.length; ++i) {
            Array.set(args, i, gestures[i].getWrappedInstance());
        }
        try {
            ReflectionUtils.invokeMethod(performGestureMethod, wrappedInstance, args);
        } catch (Exception e) {
            /* ignored */
        }

    }

    private <R> R performGestureAndWait(EventCondition<R> condition, long timeout, PointerGesture... gestures) {
        return UiAutomatorWrapper.getUiDevice()
                .performActionAndWait(new GestureRunnable(gestures), condition, timeout);
    }


    private class GestureRunnable implements Runnable {
        private final PointerGesture[] mGestures;

        public GestureRunnable(PointerGesture[] gestures) {
            mGestures = gestures;
        }

        @Override
        public void run() {
            performGesture(mGestures);
        }

        @Override
        public String toString() {
            return Arrays.toString(mGestures);
        }
    }
    

    public void click(Point point) {
        performGesture(new PointerGesture(point, gestures.getDisplayId()).pause(100L));
    }

    public void doubleClick(Point point) {
        performGesture(new PointerGesture(point, gestures.getDisplayId()).pause(0L));
        SystemClock.sleep(DEFAULT_DOUBLE_CLICK_TIMEOUT);
        performGesture(new PointerGesture(point, gestures.getDisplayId()).pause(0L));
    }

    public void longClick(Point point, @Nullable Long durationMs) {
        long duration = durationMs == null ? DEFAULT_LONG_CLICK_TIMEOUT : durationMs;
        performGesture(new PointerGesture(point, gestures.getDisplayId()).pause(duration));
    }

    public void drag(Point start, Point end, @Nullable Integer speed) {
        int dragSpeed = speed == null ? Gestures.getDefaultDragSpeed() : speed;
        performGesture(gestures.drag(start, end, dragSpeed));
    }

    public void pinchClose(Rect area, float percent, @Nullable Integer speed) {
        int pinchSpeed = speed == null ? Gestures.getDefaultPinchSpeed() : speed;
        performGesture(gestures.pinchClose(area, percent, pinchSpeed));
    }

    public void pinchOpen(Rect area, float percent, @Nullable Integer speed) {
        int pinchSpeed = speed == null ? Gestures.getDefaultPinchSpeed() : speed;
        performGesture(gestures.pinchOpen(area, percent, pinchSpeed));
    }

    public void swipe(Point start, Point end, @Nullable Integer speed) {
        int swipeSpeed = speed == null ? Gestures.getDefaultSwipeSpeed() : speed;
        performGesture(gestures.swipe(start, end, swipeSpeed));
    }

    public void swipeRect(Rect area, Direction direction, float percent, @Nullable Integer speed) {
        int swipeSpeed = speed == null ? Gestures.getDefaultSwipeSpeed() : speed;
        performGesture(gestures.swipe(area, direction, percent, swipeSpeed));
    }

    public boolean scroll(Rect area, Direction direction, float percent, @Nullable Integer speed) {
        Direction swipeDirection = Direction.reverse(direction);
        int scrollSpeed = speed == null ? Gestures.getDefaultScrollSpeed() : speed;
        for (float swipePercent = percent; swipePercent > 0.0f; swipePercent -= 1.0f) {
            float segment = Math.min(swipePercent, 2.0f);
            PointerGesture swipe = gestures.swipe(area, swipeDirection, segment, scrollSpeed).pause(250);

            // Perform the gesture and return early if we reached the end
            Boolean scrollFinishedResult = performGestureAndWait(
                    Until.scrollFinished(direction), Gestures.getScrollTimeout(), swipe
            );
            if (!Boolean.FALSE.equals(scrollFinishedResult)) {
                return false;
            }
        }
        // We never reached the end
        return true;
    }

    public boolean fling(Rect area, Direction direction, @Nullable Integer speed) {
        ViewConfiguration vc = ViewConfiguration.get(InstrumentationRegistry.getInstrumentation().getTargetContext());
        int minVelocity = vc.getScaledMinimumFlingVelocity();
        int flingSpeed = speed == null ? Gestures.getDefaultFlingSpeed() : speed;
        if (flingSpeed < minVelocity) {
            throw new IllegalArgumentException(String.format(
                    "Speed %s is less than the minimum fling velocity %s", speed, minVelocity)
            );
        }

        // To fling, we swipe in the opposite direction
        Direction swipeDirection = Direction.reverse(direction);
        PointerGesture swipe = gestures.swipe(area, swipeDirection, 1.0f, flingSpeed);

        // Perform the gesture and return true if we did not reach the end
        Boolean scrollFinishedResult = performGestureAndWait(
                Until.scrollFinished(direction),
                Gestures.getFlingTimeout(),
                swipe
        );
        return Boolean.FALSE.equals(scrollFinishedResult);
    }
}