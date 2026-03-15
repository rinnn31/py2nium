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

import android.app.UiAutomation;
import android.os.Build;
import android.os.RemoteException;
import android.util.SparseArray;
import android.view.Display;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.Nullable;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeoutException;

import io.py2nium.server.core.InteractionController;
import io.py2nium.server.core.UiAutomatorWrapper;
import io.py2nium.server.core.gestures.GestureController;
import io.py2nium.server.model.api.ByModel;
import io.py2nium.server.model.api.RotationModel;
import io.py2nium.server.utils.AxEventHelper;
import io.py2nium.server.utils.ReflectionUtils;

public class AndroidDevice {
    private static AndroidDevice INSTANCE;
    private final UiAutomation mUiAutomation;
    private final UiDevice mUiDevice;
    private final InteractionController interactionController;
    private final ElementsCache mElementsCache;

    public static synchronized AndroidDevice getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new AndroidDevice();
        }
        return INSTANCE;
    }

    public AndroidDevice() {
        this.mUiDevice = UiAutomatorWrapper.getUiDevice();
        this.mUiAutomation = UiAutomatorWrapper.getUiAutomation();
        this.interactionController = new InteractionController(mUiDevice);
        this.mElementsCache = new ElementsCache(4096);
    }

    public ElementsCache getElementsCache() {
        return mElementsCache;
    }

    @Nullable
    public Display getDisplayById(int displayId) {
        Method getDisplayByIdMethod = ReflectionUtils.getMethod(
                UiDevice.class, "getDisplayById", int.class
        );
        Display display = null;
        try {
            getDisplayByIdMethod.setAccessible(true);
            display =  (Display) getDisplayByIdMethod.invoke(mUiDevice, displayId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return display;
    }

    public int getTopmostWindowDisplayId() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            SparseArray<List<AccessibilityWindowInfo>> windowsMap = mUiAutomation.getWindowsOnAllDisplays();
            for (int i = 0; i < windowsMap.size(); i++) {
                int displayId = windowsMap.keyAt(i);
                if (displayId >= 0) {
                    return displayId;
                }
            }
        }
        return Display.DEFAULT_DISPLAY;
    }

    public InteractionController getInteractionController() {
        return interactionController;
    }

    private Object getNativeGestureController() {
        Class <?> gestureControllerClass = ReflectionUtils.getClass("androidx.test.uiautomator.GestureController");
        Method gestureControllerFactory = ReflectionUtils.getMethod(gestureControllerClass, "getInstance", UiDevice.class);

        return ReflectionUtils.invokeMethod(gestureControllerFactory, null, mUiDevice);
    }

    public GestureController getGestureController(int displayId) {
        return new GestureController(getNativeGestureController(), displayId);
    }

    public GestureController getGestureController() {
        return new GestureController(getNativeGestureController());
    }

    public void setRotation(int rotation) throws RemoteException {
        mUiAutomation.setRotation(rotation);
        Method waitRotationFinishMethod = ReflectionUtils.getMethod(UiDevice.class, "waitRotationComplete", int.class, int.class);
        ReflectionUtils.invokeMethod(waitRotationFinishMethod, mUiDevice, rotation, Display.DEFAULT_DISPLAY);
    }

    public void setRotation(int rotation, int displayId) throws RemoteException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Method rotateWithCommand = ReflectionUtils.getMethod(UiDevice.class, "rotateWithCommand", int.class, int.class);
            ReflectionUtils.invokeMethod(rotateWithCommand, mUiDevice, rotation, displayId);

            Method waitRotationFinishMethod = ReflectionUtils.getMethod(UiDevice.class, "waitRotationComplete", int.class, int.class);
            ReflectionUtils.invokeMethod(waitRotationFinishMethod, mUiDevice, rotation, displayId);
        }
    }

}
