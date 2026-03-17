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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.io.IOException;

import io.py2nium.server.common.exceptions.Py2niumException;
import io.py2nium.server.core.UiAutomatorWrapper;
import io.py2nium.server.utils.Logger;
import io.py2nium.server.utils.ShellRunner;
import io.py2nium.server.utils.StringUtils;

public class WrappedNetworkManager {

    private static WifiManager getWifiManager() {
        WifiManager wifiManager = (WifiManager) InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if(wifiManager == null) {
            throw new Py2niumException("Cannot get WifiManager");
        }
        return wifiManager;
    }
    public static boolean setWifiEnabled(boolean enabled) {
        if(isWifiEnabled() == enabled) {
            return true;
        }
        return getWifiManager().setWifiEnabled(enabled);
    }


    public static boolean connectWifi(String ssid, String password) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            Logger.w(WrappedPackageManager.class, "'connectWifi' feature is only officially supported up to Android 9. Using this feature in currently Android version may causes error.");
        }

        WifiManager wifiManager = getWifiManager();
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        if (password != null && !password.isEmpty())
            wifiConfig.preSharedKey = String.format("\"%s\"", password);
        else wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        int netId = wifiManager.addNetwork(wifiConfig);
        if (netId == -1) {
            return false;
        } else {
            return wifiManager.enableNetwork(netId, true);
        }
    }

    public static boolean isWifiEnabled() {
        return getWifiManager().isWifiEnabled();
    }

    public static boolean isAirplaneModeEnabled() {
        return Settings.Global.getInt(UiAutomatorWrapper.getAppContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static boolean toggleAirplaneMode(boolean enabled) throws IOException {
        if(isAirplaneModeEnabled() == enabled) {
            return true;
        }

        boolean hasPermission = WrappedPackageManager.isSystemApp(UiAutomatorWrapper.getAppContext().getPackageName());

        if(hasPermission) {
            Settings.Secure.putInt(UiAutomatorWrapper.getAppContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, enabled ? 1 : 0);
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", enabled);
            UiAutomatorWrapper.getAppContext().sendBroadcast(intent);
            return true;
        } else {
            Logger.i(WrappedNetworkManager.class, "No permission to toggle airplane mode. Try using root method");
            boolean ret = StringUtils.isNullOrEmpty(ShellRunner.exec("su -c 'settings put global airplane_mode_on " + (enabled ? "1'" : "0'")));
            ShellRunner.exec("su -c 'am broadcast -a android.intent.action.AIRPLANE_MODE --ez state " + enabled + "'");
            return ret;
        }
    }

    public static boolean isMobileDataEnabled() {
        ConnectivityManager connectivityManager = (ConnectivityManager) UiAutomatorWrapper.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo == null) {
            Logger.w(WrappedNetworkManager.class, "Cannot get active network info, return false by default");
            return false;
        }
        return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }
    public static boolean toggleMobileData(boolean enabled) throws IOException {
        if(isMobileDataEnabled() == enabled) {
            return true;
        }

        boolean hasPermission = UiAutomatorWrapper.getAppContext().checkCallingOrSelfPermission(Manifest.permission.CHANGE_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        if(hasPermission ) {
            TelephonyManager telephonyManager = (TelephonyManager) UiAutomatorWrapper.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.setDataEnabledForReason(TelephonyManager.DATA_ENABLED_REASON_USER, enabled);
                return true;
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.setDataEnabled(enabled);
                return true;
            }
            Logger.w(WrappedNetworkManager.class, "Permission granted but not supported in this Android version");
        }

        Logger.i(WrappedNetworkManager.class, "Cannot toggle mobile data with android API. Try using root method");
        UiAutomatorWrapper.getUiAutomation().executeShellCommand("su -c 'svc data " + (enabled ? "enable" : "disable"));

        return true;
    }
}
