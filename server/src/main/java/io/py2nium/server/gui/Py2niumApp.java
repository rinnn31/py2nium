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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import io.py2nium.server.gui.components.CardInfoItem;
import io.py2nium.server.gui.core.TestRunnerService;
import io.py2nium.server.R;
import io.py2nium.server.gui.utils.Common;

public class Py2niumApp extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_layout);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        initUI();
        checkPy2niumServerExist();
    }

    private void initUI() {
        CardInfoItem projectLinkTv = findViewById(R.id.projectLinkTextView);
        SpannableString projectLinkText = getSpannableGithubString();
        projectLinkTv.getMainTextView().setText(projectLinkText);
        projectLinkTv.getMainTextView().setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

        findViewById(R.id.settingBtn).setOnClickListener(this::onSettingBtn_click);

        findViewById(R.id.adbConnectBtn).setOnClickListener(v -> showAdbConnectionDialog());
        findViewById(R.id.rootConnectBtn).setOnClickListener(v -> startByRoot());

        findViewById(R.id.serverStatusCard).setOnClickListener(v -> {
            if (!Common.isRunningUnderTest()) {
                return;
            }
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
                    .setTitle("Do you want to stop the server?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        Intent intent = new Intent("io.py2nium.server.action.STOP");
                        sendBroadcast(intent);
                        dialog.dismiss();
                    })
                    .show();
        });

        updateServiceStatusCard(Common.isRunningUnderTest());
    }
    @NonNull
    private SpannableString getSpannableGithubString() {
        SpannableString projectLinkText = new SpannableString("Github");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(widget.getContext().getString(R.string.github_repo_url)));
                Intent chooser = Intent.createChooser(browserIntent, "Open with");
                startActivity(chooser);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        projectLinkText.setSpan(clickableSpan, 0, projectLinkText.length(), 0);
        return projectLinkText;
    }

    private void updateServiceStatusCard(boolean state) {
        CardInfoItem serviceStateTv = findViewById(R.id.serverStatusCard);
        CardInfoItem adbConnectBtn = findViewById(R.id.adbConnectBtn);
        CardInfoItem rootConnectBtn = findViewById(R.id.rootConnectBtn);

        if(state) {
            serviceStateTv.getIconView().setImageResource(R.drawable.running_icon);
            ((CardView)serviceStateTv.getIconView().getParent()).setCardBackgroundColor(getResources().getColor(R.color.lightGreen));
            serviceStateTv.getMainTextView().setText(getString(R.string.server_running_status));
            serviceStateTv.getSubTextView().setText(getString(R.string.running_instrument));

            adbConnectBtn.setClickable(false);
            rootConnectBtn.setClickable(false);
        } else {
            serviceStateTv.getIconView().setImageResource(R.drawable.stopped_icon);
            ((CardView)serviceStateTv.getIconView().getParent()).setCardBackgroundColor(getResources().getColor(R.color.lightRed));
            serviceStateTv.getMainTextView().setText(getString(R.string.server_stopped_status));
            serviceStateTv.getSubTextView().setText(getString(R.string.stopped_instrument));

            adbConnectBtn.setClickable(true);
            rootConnectBtn.setClickable(true);
        }
    }
    public void onSettingBtn_click(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showAdbConnectionDialog() {
        Runnable runnable = () -> {
            View dialogView = getLayoutInflater().inflate(R.layout.adb_connect_view, null);
            MaterialButton connectBtn = dialogView.findViewById(R.id.connectButton);
            MaterialButton cancelBtn = dialogView.findViewById(R.id.cancelButton);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);
            builder.setView(dialogView);

            runOnUiThread(() -> {
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                connectBtn.setOnClickListener(v -> {
                    String port = ((TextInputLayout)dialogView.findViewById(R.id.ipTextBox)).getEditText().getText().toString();
                    String pairCode = ((TextInputLayout)dialogView.findViewById(R.id.pairingCodeTextBox)).getEditText().getText().toString();

                    if(port.isEmpty()) {
                        ((TextInputLayout)dialogView.findViewById(R.id.ipTextBox)).setError("Port is required");
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putInt("method", TestRunnerService.ADB_METHOD);
                        bundle.putInt("adbPort", Integer.parseInt(port));
                        if(!pairCode.isEmpty()) {
                            bundle.putString("pairCode", pairCode);
                        }

                        startServer(bundle);
                        dialog.dismiss();
                    }


                });

                cancelBtn.setOnClickListener(v -> {
                    dialog.dismiss();
                });
            });
        };
        new Thread(runnable).start();

    }

    private void startByRoot() {
        Runnable runnable = () -> {
            SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.SETTINGS_NAME, MODE_PRIVATE);
            String binary = sharedPreferences.getString("suBinaryPath", null);
            if(binary == null) {
                binary = Common.findSuBinary();
            }

            String finalBinary = binary;
            runOnUiThread(() -> {
                if(finalBinary == null || !Common.isBinaryExist(finalBinary)) {
                    new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
                            .setTitle("Error")
                            .setMessage("Root binary not found")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putInt("method", TestRunnerService.ROOT_METHOD);
                    bundle.putString("suBinaryPath", finalBinary);
                    startServer(bundle);
                }
            });

        };
        new Thread(runnable).start();
    }

    private void startServer(Bundle bundle) {
        Intent intent = new Intent(this, TestRunnerService.class);
        intent.putExtras(bundle);
        startService(intent);
    }

    private void checkPy2niumServerExist() {
        PackageManager pm = getPackageManager();

        try {
            pm.getApplicationInfo("io.py2nium.server.test", 0);
        } catch (PackageManager.NameNotFoundException e) {
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
                    .setTitle("Error")
                    .setMessage("Py2nium server test apk not found")
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    })
                    .show();

        }
    }
}
