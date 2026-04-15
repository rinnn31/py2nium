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

import io.netty.handler.codec.http.HttpMethod;
import io.py2nium.server.core.Py2niumService;
import io.py2nium.server.core.UiAutomatorWrapper;
import io.py2nium.server.handler.apps.GetApplicationInfoCommand;
import io.py2nium.server.handler.apps.GetPackagesCommand;
import io.py2nium.server.handler.apps.InstallAppCommand;
import io.py2nium.server.handler.clipboard.ClearClipboardCommand;
import io.py2nium.server.handler.clipboard.GetClipboardCommand;
import io.py2nium.server.handler.clipboard.SetClipboardCommand;
import io.py2nium.server.handler.device.DumpSourceCommand;
import io.py2nium.server.handler.device.WaitEventCommand;
import io.py2nium.server.handler.device.ExecuteShellCommand;
import io.py2nium.server.handler.device.GetDeviceInfoCommand;
import io.py2nium.server.handler.device.MakeToastCommand;
import io.py2nium.server.handler.device.NetworkCommand;
import io.py2nium.server.handler.device.PerformGlobalActionCommand;
import io.py2nium.server.handler.device.SendKeyCommand;
import io.py2nium.server.handler.device.SendKeysCommand;
import io.py2nium.server.handler.device.SetRotationCommand;
import io.py2nium.server.handler.capture.TakeScreenshotCommand;
import io.py2nium.server.handler.device.QueryEventCommand;
import io.py2nium.server.handler.elements.ClickCommand;
import io.py2nium.server.handler.elements.DeleteCommand;
import io.py2nium.server.handler.elements.DoubleClickCommand;
import io.py2nium.server.handler.elements.DragCommand;
import io.py2nium.server.handler.elements.ElementChildrenCommand;
import io.py2nium.server.handler.elements.FindElementCommand;
import io.py2nium.server.handler.elements.FindElementsCommand;
import io.py2nium.server.handler.elements.FlingCommand;
import io.py2nium.server.handler.elements.GetAttributeCommand;
import io.py2nium.server.handler.elements.GetElementParentCommand;
import io.py2nium.server.handler.elements.PerformAccessibilityActionCommand;
import io.py2nium.server.handler.elements.PinchCloseCommand;
import io.py2nium.server.handler.elements.PinchOpenCommand;
import io.py2nium.server.handler.elements.RefreshCommand;
import io.py2nium.server.handler.elements.SetTextCommand;
import io.py2nium.server.handler.elements.SwipeCommand;
import io.py2nium.server.http.HttpServerImpl;
import io.py2nium.server.utils.AxEventHelper;

public class Py2niumAutomatorStub {
    private final HttpServerImpl mHttpServer;

    private boolean mStopQuerying = false;

    private final KillSignalReceiver mPingPongReceiver = new KillSignalReceiver();

    public Py2niumAutomatorStub(String host, int port) {
        mHttpServer = new HttpServerImpl(host, port);
        initHandler();
    }

    private void initHandler() {
        mHttpServer.addHandler(new GetApplicationInfoCommand("/app/info", HttpMethod.POST));
        mHttpServer.addHandler(new GetPackagesCommand("/app/packages", HttpMethod.GET));
        mHttpServer.addHandler(new InstallAppCommand("/app/install", HttpMethod.POST));

        mHttpServer.addHandler(new TakeScreenshotCommand("/capture/take", HttpMethod.POST));

        mHttpServer.addHandler(new GetClipboardCommand("/device/clipboard/get", HttpMethod.GET));
        mHttpServer.addHandler(new SetClipboardCommand("/device/clipboard/set", HttpMethod.POST));
        mHttpServer.addHandler(new ClearClipboardCommand("/device/clipboard/clear", HttpMethod.POST));
        mHttpServer.addHandler(new ExecuteShellCommand("/device/shell", HttpMethod.POST));
        mHttpServer.addHandler(new DumpSourceCommand("/device/source", HttpMethod.GET));
        mHttpServer.addHandler(new SendKeysCommand("/device/keys", HttpMethod.POST));
        mHttpServer.addHandler(new SendKeyCommand("/device/key", HttpMethod.POST));
        mHttpServer.addHandler(new SetRotationCommand("/device/rotation", HttpMethod.POST));
        mHttpServer.addHandler(new NetworkCommand("/device/network", HttpMethod.POST));
        mHttpServer.addHandler(new PerformGlobalActionCommand("/device/global_action", HttpMethod.GET));
        mHttpServer.addHandler(new MakeToastCommand("/device/toast", HttpMethod.POST));
        mHttpServer.addHandler(new QueryEventCommand("/device/event/query", HttpMethod.POST));
        mHttpServer.addHandler(new WaitEventCommand("/device/event/wait", HttpMethod.POST));
        mHttpServer.addHandler(new GetDeviceInfoCommand("/device/info", HttpMethod.GET));

        mHttpServer.addHandler(new FindElementCommand("/elements/find_element", HttpMethod.POST));
        mHttpServer.addHandler(new FindElementCommand("/elements/:element/find_element", HttpMethod.POST));
        mHttpServer.addHandler(new FindElementsCommand("/elements/find_elements", HttpMethod.POST));
        mHttpServer.addHandler(new FindElementsCommand("/elements/:element/find_elements", HttpMethod.POST));
        mHttpServer.addHandler(new ElementChildrenCommand("/elements/:element/children", HttpMethod.GET));
        mHttpServer.addHandler(new GetElementParentCommand("/elements/:element/parent", HttpMethod.GET));
        mHttpServer.addHandler(new RefreshCommand("/elements/:element/refresh", HttpMethod.GET));
        mHttpServer.addHandler(new DeleteCommand("/elements/:element/delete", HttpMethod.GET));
        mHttpServer.addHandler(new GetAttributeCommand("/elements/:element/attribute/:attribute", HttpMethod.GET));
        mHttpServer.addHandler(new ClickCommand("/elements/:element/click", HttpMethod.POST, HttpMethod.GET));
        mHttpServer.addHandler(new DoubleClickCommand("/elements/:element/double_click", HttpMethod.POST, HttpMethod.GET));
        mHttpServer.addHandler(new DragCommand("/elements/:element/drag", HttpMethod.POST));
        mHttpServer.addHandler(new SwipeCommand("/elements/:element/swipe", HttpMethod.POST));
        mHttpServer.addHandler(new FlingCommand("/elements/:element/fling", HttpMethod.POST));
        mHttpServer.addHandler(new PinchOpenCommand("/elements/:element/pinch_open", HttpMethod.POST));
        mHttpServer.addHandler(new PinchCloseCommand("/elements/:element/pinch_close", HttpMethod.POST));
        mHttpServer.addHandler(new SetTextCommand("/elements/:element/text", HttpMethod.POST));
        mHttpServer.addHandler(new PerformAccessibilityActionCommand("/elements/:element/action", HttpMethod.POST));

        mHttpServer.addHandler(new io.py2nium.server.handler.gestures.ClickCommand("/gestures/click", HttpMethod.POST));
        mHttpServer.addHandler(new io.py2nium.server.handler.gestures.DoubleClickCommand("/gestures/double_click", HttpMethod.POST));
        mHttpServer.addHandler(new io.py2nium.server.handler.gestures.SwipeCommand("/gestures/swipe", HttpMethod.POST));
        mHttpServer.addHandler(new io.py2nium.server.handler.gestures.GesturesCommand("/gestures/perform", HttpMethod.POST));
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
