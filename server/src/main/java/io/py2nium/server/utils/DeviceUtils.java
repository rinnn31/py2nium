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

import android.graphics.Point;
import android.os.Build;

import androidx.test.uiautomator.UiDevice;

import java.lang.reflect.Method;

import io.py2nium.server.core.UiAutomatorWrapper;
import io.py2nium.server.model.api.DeviceInfoModel;

public class DeviceUtils {
    public static Point getDisplaySize(int displayId) {
        Method getDisplaySizeMethod = ReflectionUtils.getMethod(
                UiDevice.class, "getDisplaySize", int.class
        );
        try {
            return (Point) getDisplaySizeMethod.invoke(UiAutomatorWrapper.getUiDevice(), displayId);
        } catch (Exception e) {
            /*ignored*/
            return new Point(0,0);
        }
    }
    public static DeviceInfoModel getDeviceInfo() {
        DeviceInfoModel info = new DeviceInfoModel();
        Point screenSize = getDisplaySize(0);

        info.sdk = Build.VERSION.SDK_INT;
        info.os = Build.VERSION.RELEASE;
        info.manufacturer = Build.MANUFACTURER;
        info.model = Build.MODEL;
        info.product = Build.PRODUCT;
        info.brand = Build.BRAND;
        info.screenSize = screenSize.x + "x" + screenSize.y;

        return info;
    }


}
