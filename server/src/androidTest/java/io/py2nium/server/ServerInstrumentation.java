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

import android.os.Bundle;
import android.os.SystemClock;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerInstrumentation {
    static {
        System.loadLibrary("py2nium-extensions");
    }

    Py2niumAutomatorStub stub;

    @Before
    public void setUp() {
        Bundle bundle = InstrumentationRegistry.getArguments();
        int port = bundle.getInt("port", 8080);
        String host = bundle.getString("host", "127.0.0.1");
        stub = new Py2niumAutomatorStub(host, port);
        stub.start();
    }

    @Test
    @LargeTest
    public void runInstrumentation() {
        while(stub.isAlive()) {
            SystemClock.sleep(1000);
        }
    }

    @After
    public void tearDown() {
        stub.cleanUp();
    }
}
