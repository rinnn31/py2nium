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

import android.os.Build;

import androidx.annotation.NonNull;

import java.security.PrivateKey;
import java.security.cert.Certificate;

import io.github.muntashirakon.adb.AbsAdbConnectionManager;
import io.py2nium.server.gui.utils.KeyStoringHelper;

public class AdbConnectionManagerImpl extends AbsAdbConnectionManager {
    static final String ADB_KEY_ALIAS = "py2nium-key";

    private PrivateKey mPrivateKey;
    private Certificate mCertificate;

    private static AdbConnectionManagerImpl INSTANCE;

    public synchronized static AdbConnectionManagerImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdbConnectionManagerImpl();
        }
        return INSTANCE;
    }

    private AdbConnectionManagerImpl() {
        setApi(Build.VERSION.SDK_INT);
        // Load the private key and certificate
        mPrivateKey = KeyStoringHelper.getPrivateKey(ADB_KEY_ALIAS, null);
        if(mPrivateKey == null) {
            if(KeyStoringHelper.generateNewInfo(ADB_KEY_ALIAS, null)) {
                mPrivateKey = KeyStoringHelper.getPrivateKey(ADB_KEY_ALIAS, null);
                mCertificate = KeyStoringHelper.getCertificate(ADB_KEY_ALIAS);
            } else {
                throw new RuntimeException("Failed to generate new key pair.");
            }
        } else {
            mCertificate = KeyStoringHelper.getCertificate(ADB_KEY_ALIAS);
        }

    }
    @NonNull
    @Override
    protected PrivateKey getPrivateKey() {
        return mPrivateKey;
    }

    @NonNull
    @Override
    protected Certificate getCertificate() {
        return mCertificate;
    }

    @NonNull
    @Override
    protected String getDeviceName() {
        return "Py2nium Device";
    }
}
