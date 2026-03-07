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

package io.py2nium.server;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.core.content.ContextCompat;

import io.py2nium.server.core.Py2niumService;
import io.py2nium.server.core.UiAutomatorWrapper;
import io.py2nium.server.http.HttpServerImpl;

public class Py2niumAutomatorStub {
    private final HttpServerImpl mHttpServer;

    private boolean mStopQuerying = false;

    private final KillSignalReceiver mPingPongReceiver = new KillSignalReceiver();

    public Py2niumAutomatorStub(String host, int port) {
        mHttpServer = new HttpServerImpl(host, port);
        initHandler();
    }

    private void initHandler() {
    }

    public void start() {
        Intent intent = new Intent(UiAutomatorWrapper.getAppContext(), Py2niumService.class);
        intent.putExtra("host", mHttpServer.getHost());
        intent.putExtra("port", mHttpServer.getPort());
        ContextCompat.startForegroundService(UiAutomatorWrapper.getAppContext(), intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(KillSignalReceiver.STOP_ACTION);
        ContextCompat.registerReceiver(UiAutomatorWrapper.getAppContext(), mPingPongReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        mHttpServer.startServer();
    }

    public void cleanUp() {
        UiAutomatorWrapper.getAppContext().unregisterReceiver(mPingPongReceiver);
        mHttpServer.stopServer();
    }

    public boolean isAlive() {
        return !mStopQuerying && mHttpServer.isAlive();
    }

    /*
    Create communication channel for Py2nium app to check this stub's state
    and stop it when needed without shell/root privileges.
    */
    private class KillSignalReceiver extends BroadcastReceiver {
        static final String STOP_ACTION = "io.py2nium.server.action.STOP";

        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            String action = intent.getAction();
            if (STOP_ACTION.equals(action)) {
                mStopQuerying = true;
            }
        }
    }
}
