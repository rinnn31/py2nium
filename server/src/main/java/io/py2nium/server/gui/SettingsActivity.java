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


package io.py2nium.server.gui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputEditText;

import io.py2nium.server.R;

public class SettingsActivity extends AppCompatActivity {
    public static final String SETTINGS_NAME = "Py2niumSettings";

    TextInputEditText mServerPortEditText;

    TextInputEditText mSuBinaryPathEditText;

    TextInputEditText mAdbPortEditText;

    SwitchCompat mStartOnBootSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.hide();

        mServerPortEditText = findViewById(R.id.serverPort);
        mSuBinaryPathEditText = findViewById(R.id.suPath);
        mStartOnBootSwitch = findViewById(R.id.startOnBootSwitch);
        mAdbPortEditText = findViewById(R.id.adbDefaultPort);

        findViewById(R.id.saveBtn).setOnClickListener(this::onSaveBtn_click);
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        loadSettings();
    }

    private void onSaveBtn_click(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String serverPortText = mServerPortEditText.getText().toString();
        int serverPort = serverPortText.isEmpty() ? 0 : Integer.parseInt(serverPortText);
        if(serverPort < 1 || serverPort > 65535) {
            mServerPortEditText.setError("Invalid port number. Must be between 1 and 65535");
            return;
        }

        String adbPortText = mAdbPortEditText.getText().toString();
        int adbPort = adbPortText.isEmpty() ? 0 : Integer.parseInt(adbPortText);
        if(adbPort < 1 || adbPort > 65535) {
            mAdbPortEditText.setError("Invalid port number. Must be between 1 and 65535");
            return;
        }


        editor.putInt("serverPort", serverPort);
        editor.putInt("adbPort", adbPort);
        editor.putString("suBinaryPath", mSuBinaryPathEditText.getText().toString());
        editor.putBoolean("startOnBoot", mStartOnBootSwitch.isChecked());

        editor.apply();

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);

        mServerPortEditText.setText(String.valueOf(sharedPreferences.getInt("serverPort", 8080)));
        mAdbPortEditText.setText(String.valueOf(sharedPreferences.getInt("adbPort", 5555)));
        mSuBinaryPathEditText.setText(sharedPreferences.getString("suBinaryPath", ""));
        mStartOnBootSwitch.setChecked(sharedPreferences.getBoolean("startOnBoot", false));
    }
}