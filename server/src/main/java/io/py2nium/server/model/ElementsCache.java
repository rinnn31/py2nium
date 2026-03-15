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


package io.py2nium.server.model;

import android.util.LruCache;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.py2nium.server.common.exceptions.ElementNotFoundException;

public class ElementsCache {
    private final LruCache<String, AndroidElement> mCache;

    public ElementsCache(int maxSize) {
        mCache = new LruCache<>(maxSize);
    }

    public AndroidElement getElement(String UUID) {
        AndroidElement result = mCache.get(UUID);
        if(result == null) {
            throw new ElementNotFoundException(UUID);
        }
        return result;
    }


    public String putElement(@NonNull AndroidElement element) {
        String UUID = element.getUUID();
        mCache.put(UUID, element);
        return UUID;
    }

    public void clearCache() {
        mCache.evictAll();
    }

    public void remove(String UUID) {
        AndroidElement element = mCache.get(UUID);
        if(element == null) {
            throw new ElementNotFoundException(UUID);
        }
        mCache.remove(UUID);

    }


}
