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


package io.py2nium.server.core;

import static io.py2nium.server.utils.ReflectionUtils.getMethod;
import static io.py2nium.server.utils.ReflectionUtils.invokeMethod;

import android.os.SystemClock;
import android.view.InputEvent;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

public class InteractionController {
    @NonNull
    private final Object nativeICObject;
    private static final String TOUCH_UP = "touchUp";
    private static final String TOUCH_DOWN = "touchDown";
    private static final String TOUCH_MOVE = "touchMove";
    private static final String SEND_KEYS = "sendKeys";
    private static final String SEND_KEY = "sendKey";

    private static final String INJECT_EVENT_SYNC = "injectEventSync";


    public InteractionController(@NonNull Object interactionController) {
        nativeICObject = interactionController;
    }

    public boolean touchUp(int x, int y) {
        Method method = getMethod(nativeICObject.getClass(), TOUCH_UP, int.class, int.class);
        return (boolean) invokeMethod(method, nativeICObject, x, y);
    }

    public boolean touchDown(int x, int y) {
        Method method = getMethod(nativeICObject.getClass(), TOUCH_DOWN, int.class, int.class);
        return (boolean) invokeMethod(method, nativeICObject, x, y);
    }

    public boolean touchMove(int x, int y) {
        Method method = getMethod(nativeICObject.getClass(), TOUCH_MOVE, int.class, int.class);
        return (boolean) invokeMethod(method, nativeICObject, x, y);
    }
    public boolean sendKeys(int[] keyCodes, int metaState) {
        Method method = getMethod(nativeICObject.getClass(), SEND_KEYS, int[].class, int.class);
        return (boolean) invokeMethod(method, nativeICObject, keyCodes, metaState);
    }

    public boolean sendKey(int keyCode, int metaState) {
        Method method = getMethod(nativeICObject.getClass(), SEND_KEY, int.class, int.class);
        return (boolean) invokeMethod(method, nativeICObject, keyCode, metaState);
    }

    public boolean injectEventSync(InputEvent event) {
        Method method = getMethod(nativeICObject.getClass(), INJECT_EVENT_SYNC, InputEvent.class);
        return (boolean) invokeMethod(method, nativeICObject, event);
    }

    public void wait(int duration) {
        SystemClock.sleep(duration);
    }


}
