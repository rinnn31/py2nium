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


package io.py2nium.server.utils.service;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Printer;
import android.util.StringBuilderPrinter;

import androidx.test.platform.app.InstrumentationRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.py2nium.server.common.exceptions.Py2niumException;
import io.py2nium.server.model.api.AppInfo;
import io.py2nium.server.utils.Logger;

public class WrappedPackageManager {
    private static PackageManager getPackageManager() {
        PackageManager packageManager = (PackageManager) InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getApplicationContext()
                .getPackageManager();
        if(packageManager == null) {
            throw new Py2niumException("Cannot get PackageManager");
        }
        return packageManager;
    }

    public static AppInfo getApplicationInfo(String packageName, boolean isDumpInfo) throws PackageManager.NameNotFoundException {
        PackageManager pm = getPackageManager();
        PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
        String[] activities = null;
        String[] services = null;
        if (pi.activities != null) {
            activities = new String[pi.activities.length];
            for (int i = 0; i < pi.activities.length; i++) {
                activities[i] = pi.activities[i].name;
            }
        }
        if (pi.services != null) {
            services = new String[pi.services.length];
            for (int i = 0; i < pi.services.length; i++) {
                services[i] = pi.services[i].name;
            }
        }

        String outDump = null;
        if(isDumpInfo) {
            StringBuilder strBuilder =  new StringBuilder();
            Printer printer = new StringBuilderPrinter(strBuilder);
            pi.applicationInfo.dump(printer, "");
            outDump = strBuilder.toString();
        }

        return new AppInfo(packageName,
                           pi.applicationInfo.loadLabel(pm).toString(),
                           pi.applicationInfo.uid,
                           activities,
                           services,
                           outDump);
    }

    public static List<String> getPackages(int queryFlags) {
        List<String> packages = new ArrayList<>();

        PackageManager pm = getPackageManager();
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            Logger.w(WrappedPackageManager.class, "getPackages(int queryFlags) won't return all application info since API level 30");
        }

        for(ApplicationInfo app : pm.getInstalledApplications(0)) {
            packages.add(app.packageName);
        }
        return packages;
    }

    public static boolean isSystemApp(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 || (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(WrappedPackageManager.class, "Package not found: " + packageName, e);
            return false;
        }
    }
}
