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


package io.py2nium.server.utils.service;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;

import java.util.Arrays;
import java.util.List;

import io.py2nium.server.common.exceptions.FeatureNotSupportedException;
import io.py2nium.server.common.exceptions.Py2niumException;
import io.py2nium.server.model.api.ClipBoardModel;

public class WrappedClipboardManager {

    private static ClipboardManager getClipboardManager() {
        ClipboardManager manager =  (ClipboardManager) InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if(manager == null) {
            throw new Py2niumException("Cannot get ClipboardManager");
        }
        return manager;
    }
    public static ClipBoardModel getContent() {
        ClipboardManager clipboardManager = getClipboardManager();

        if(clipboardManager.hasPrimaryClip() && clipboardManager.getPrimaryClip().getItemCount() > 0) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
            if(item.getText() != null) {
                return new ClipBoardModel(SupportedDataType.PLAIN_TEXT.toString(), item.getText().toString());
            }
            if(item.getUri() != null) {
                return new ClipBoardModel(SupportedDataType.URI.toString(), item.getUri().toString());
            }
        }
        return null;
    }

    public static boolean setContent(ClipBoardModel input) {
        ClipboardManager clipboardManager = getClipboardManager();

        ClipData clipData;
        switch (input.dataType) {
            case "PLAIN_TEXT":
                clipData = ClipData.newPlainText("data", input.data);
                break;
            case "URI":
                ContentResolver resolver = InstrumentationRegistry.getInstrumentation().getContext().getContentResolver();
                clipData = ClipData.newUri(resolver, "data", Uri.parse(input.data));
                break;
            default:
                return false;
        }

        clipboardManager.setPrimaryClip(clipData);
        return true;
    }
    public static void clearClipboard() {
        ClipboardManager clipboardManager = getClipboardManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboardManager.clearPrimaryClip();
        } else {
            throw new FeatureNotSupportedException("This feature is only support for Android 9 (SDK 28) and above.", 28, null);
        }
    }
    public enum SupportedDataType {
        PLAIN_TEXT,
        URI;

        public static List<String> getSupportedType() {
            return Arrays.asList(PLAIN_TEXT.toString(), URI.toString());
        }
    }
}
