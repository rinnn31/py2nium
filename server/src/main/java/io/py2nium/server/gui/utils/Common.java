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


package io.py2nium.server.gui.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Common {
    private static final String[] SU_BINARY_PATHS = {"/system/bin/su", "/system/xbin/su", "/sbin/su", "/su/bin/su"};

    public static String findSuBinary() {
        String suPath = null;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/system/bin/which", "su"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if(line != null && line.length() > 0) {
                suPath = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(suPath == null) {

            for(String path : SU_BINARY_PATHS) {
                if(isBinaryExist(path)) {
                    suPath = path;
                    break;
                }
            }
        }

        return suPath;
    }

    public static boolean isBinaryExist(String path) {
        File file = new File(path);
        return file.exists() && file.canExecute();
    }

    public static boolean isRunningUnderTest() {
        boolean isTest;
        try {
            Class.forName("androidx.test.runner.AndroidJUnitRunner");
            isTest = true;
        } catch (ClassNotFoundException e) {
            isTest = false;
        }
        return isTest;
    }
}
