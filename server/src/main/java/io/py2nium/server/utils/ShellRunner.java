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

import android.os.Environment;

import androidx.test.uiautomator.UiDevice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.py2nium.server.core.UiAutomatorWrapper;

/* UiAutomation.execShellCommand() uses Runtime.exec to execute command,
 * it doesn't support quoting or escaping arguments. So we need to handle shell commands
 * by writing command to a script file and executing that script.
 */
public class ShellRunner {

    private static synchronized String createScript(String command) {
        long timestamp = System.currentTimeMillis();
        String scriptPath = "/data/local/tmp/shell_" + timestamp + ".sh";
        String tempScriptPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/py2temp/shell_" + timestamp + ".sh";
        try {
            File temp = new File(tempScriptPath);
            temp.createNewFile();
            try (FileWriter writer = new FileWriter(temp)) {
                writer.write(command + "\n");
            }
            UiDevice uiDevice = UiAutomatorWrapper.getUiDevice();
            uiDevice.executeShellCommand("mv " + tempScriptPath + " " + scriptPath);
            uiDevice.executeShellCommand("chmod 755 " + scriptPath);
            uiDevice.executeShellCommand("rm -f " + tempScriptPath);

            return scriptPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String exec(String command) throws IOException {
        String scriptPath = createScript(command);
        String ret = UiAutomatorWrapper.getUiDevice().executeShellCommand("sh " + scriptPath);
        UiAutomatorWrapper.getUiDevice().executeShellCommand("rm -f " + scriptPath);
        return ret;
    }
}
