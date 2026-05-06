/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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


package io.py2nium.server.gui.core;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

public class TestRunnerService extends Service {
    public final static int ADB_METHOD = 1;

    public final static int ROOT_METHOD = 2;

    public final static int AUTO_METHOD = 0;

    private static final String TAG = "Py2niumRunnerService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer(intent.getExtras());
        return START_NOT_STICKY;
    }

    private void startServer(Bundle bundle) {
        // Stop any existing server first
        Intent stopIntent = new Intent("io.py2nium.server.STOP");
        sendBroadcast(stopIntent);
        SystemClock.sleep(2000); // Wait for 2 seconds to ensure the server is stopped

        IShellProvider provider = getShellProvider(bundle);
        if(provider == null) {
            Log.e(TAG, "Failed to get shell provider. Stopping...");
            return;
        }
        Log.i(TAG ,"Shell provider initialized. Getting server configurations...");

        int port = bundle.getInt("serverPort", 8080);

        Log.i(TAG, "Starting Py2nium server on port " + port + "...");
        @SuppressLint("DefaultLocale")
        String command = String.format("nohup am instrument -w -e port %d io.py2nium.server.test/androidx.test.runner.AndroidJUnitRunner > /dev/null 2>&1 &",
                                port);
        provider.execNew(command);
    }

    private IShellProvider getShellProvider(Bundle bundle) {
        int method = bundle.getInt("method", AUTO_METHOD);
        int adbPort = bundle.getInt("adbPort", 0);
        String pairCode = bundle.getString("pairCode");
        String binaryPath = bundle.getString("suBinaryPath");

        switch (method) {
            case ADB_METHOD:
                if(adbPort == 0) {
                    Log.e(TAG, "ADB method requires a port number.");
                    return null;
                }
                return tryGetAdbProvider(adbPort, pairCode);
            case ROOT_METHOD:
                if(binaryPath == null) {
                    Log.e(TAG, "ROOT method requires a su binary path.");
                    return null;
                }
                return tryGetRootProvider(binaryPath);
            case AUTO_METHOD:
                IShellProvider provider = tryGetAdbProvider(adbPort, pairCode);
                if(provider == null) {
                    Log.e(TAG, "Failed to get ADB provider. Trying ROOT method...");
                    provider = tryGetRootProvider(binaryPath);

                    if(provider == null) {
                        Log.e(TAG, "Failed to get ROOT provider.");
                    }
                }
                return provider;
            default:
                Log.e(TAG, "Invalid method. Only ADB(0) and ROOT(1) or AUTO(2) methods are supported.");
                return null;
        }
    }

    private AdbShellProvider tryGetAdbProvider(int port, @Nullable String pairCode) {
        Log.i(TAG, "Using ADB method. Getting ADB provider...");
        AdbConnectionManagerImpl connectionManager = AdbConnectionManagerImpl.getInstance();
        if(!connectionManager.isConnected()) {
            Log.i(TAG, "The connection to adbd is not established. Trying to connect...");

            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || pairCode == null || pairCode.isEmpty()) {
                    connectionManager.connect(port);
                } else {
                    connectionManager.pair(port, pairCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to connect to adbd", e);
            }
        }

        if(!connectionManager.isConnected()) {
            Log.e(TAG, "Failed to connect to adbd.");
            return null;
        }
        Log.i(TAG, "Connected to adbd.");

        return new AdbShellProvider(connectionManager.getAdbConnection());
    }

    private RuntimeShellProvider tryGetRootProvider(String suBinaryPath) {
        Log.i(TAG, "Using ROOT method. Getting ROOT provider...");
        if(suBinaryPath == null) {
            Log.e(TAG, "Su binary path or any shell binary that can run commands as root is required.");
            return null;
        }
        return new RuntimeShellProvider(suBinaryPath);
    }

}
