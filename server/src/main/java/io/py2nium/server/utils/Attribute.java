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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum Attribute {
    CHECKABLE("checkable"),
    CHECKED("checked"),
    CLASS("class"),
    CLICKABLE("clickable"),
    CONTENT_DESC("content-desc"),
    ENABLED("enabled"),
    FOCUSABLE("focusable"),
    FOCUSED("focused"),
    LONG_CLICKABLE("long-clickable"),
    PACKAGE("package"),
    PASSWORD("password"),
    RESOURCE_ID("resource-id"),
    SCROLLABLE("scrollable"),
    SELECTED("selected"),
    TEXT("text"),
    HINT("hint"),
    BOUNDS("bounds"),
    INDEX("index"),
    DISPLAYED("displayed");

    private final String alias;
    Attribute(String alias) {
        this.alias = alias;
    }

    @NonNull
    @Override
    public String toString() {
        return alias;
    }

    @Nullable
    public static Attribute fromString(String attr) {
        for(Attribute attribute: values()) {
            if(attribute.alias.equalsIgnoreCase(attr)) {
                return attribute;
            }
        }
        return null;
    }
}
