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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import io.py2nium.server.gui.core.TestRunnerService;
import io.py2nium.server.gui.SettingsActivity;

public class BootupListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsActivity.SETTINGS_NAME, Context.MODE_PRIVATE);
            if(sharedPreferences.getBoolean("startOnBoot", false)) {
                Bundle bundle = new Bundle();
                bundle.putInt("method", TestRunnerService.AUTO_METHOD);
                bundle.putInt("serverPort", sharedPreferences.getInt("serverPort", 8080));
                bundle.putInt("adbPort", sharedPreferences.getInt("adbPort", 5555));
                bundle.putString("suBinaryPath", sharedPreferences.getString("suBinaryPath", ""));

                Intent serviceIntent = new Intent(context, TestRunnerService.class);
                serviceIntent.putExtras(bundle);
                context.startService(serviceIntent);
            }
        }
    }
}
