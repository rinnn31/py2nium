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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import io.py2nium.server.utils.Logger;
import io.py2nium.server.utils.ReflectionUtils;

public class PointerGesture {
    private final static Class<?> wrappedClass = ReflectionUtils.getClass("androidx.test.uiautomator.PointerGesture");
    private Object wrappedInstance;
    private Method pauseMethod;
    private Method moveMethod;

    public PointerGesture(Point startPoint, int displayId) {
        this(startPoint, 0, displayId);
    }

    public PointerGesture(Point startPoint, long initialDelay, int displayId) {
        try {
            Constructor<?> constructor = wrappedClass.getConstructor(Point.class, long.class, int.class);
            constructor.setAccessible(true);
            wrappedInstance = constructor.newInstance(startPoint, initialDelay, displayId);
            extractMethods();
        } catch (Exception e) {
            Logger.e(PointerGesture.class, "Cannot create native PointerGesture instance.", e);
        }
    }

    public PointerGesture(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
        extractMethods();
    }

    private void extractMethods() {
        pauseMethod = ReflectionUtils.getMethod(wrappedClass, "pause", long.class);
        moveMethod = ReflectionUtils.getMethod(wrappedClass, "move", Point.class, int.class);
    }

    public PointerGesture pause(long time) {
        if(wrappedInstance != null && pauseMethod != null){
            try {
                ReflectionUtils.invokeMethod(pauseMethod, wrappedInstance, time);
            } catch (Exception e) {
                /* ignored */
            }
        }
        return this;
    }

    public PointerGesture move(Point dest, int speed) {
        if(wrappedInstance != null && moveMethod != null){
            try {
                ReflectionUtils.invokeMethod(moveMethod, wrappedInstance, dest, speed);
            } catch (Exception e) {
                /* ignored */
            }
        }
        return this;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    public static Class<?> getWrappedClass() {
        return wrappedClass;
    }
}
