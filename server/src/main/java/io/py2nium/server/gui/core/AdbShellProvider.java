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

import io.github.muntashirakon.adb.AdbConnection;
import io.github.muntashirakon.adb.AdbStream;

public class AdbShellProvider implements IShellProvider {
    private final AdbConnection mAdbConnection;

    public AdbShellProvider(AdbConnection adbConnection) {
        this.mAdbConnection = adbConnection;
    }

    @Override
    public void execNew(String command) {
        try {
            AdbStream stream = mAdbConnection.open("shell:"+command+"\n");
            stream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
