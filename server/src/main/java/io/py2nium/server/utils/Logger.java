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

import android.util.Log;
public class Logger {
    private static final String TAG = "PY2NIUM-SERVER";

    public static void i(Class<?> source, String message) {
        if (Log.isLoggable(TAG, Log.INFO)) Log.i(TAG, String.format("[%s]: %s", source.getSimpleName(), message));
    }


    public static void e(Class<?> source, String message, Throwable throwable) {
        if (Log.isLoggable(TAG, Log.ERROR))  android.util.Log.e(TAG, String.format("[%s]: %s", source.getSimpleName(), message), throwable);
    }

    public static void e(Class<?> source, String message) {
        if (Log.isLoggable(TAG, Log.ERROR)) android.util.Log.e(TAG, String.format("[%s]: %s", source.getSimpleName(), message));
    }

    public static void d(Class<?> source, String message) {
        if (Log.isLoggable(TAG, Log.DEBUG))  android.util.Log.d(TAG, String.format("[%s]: %s", source.getSimpleName(), message));
    }

    public static void w(Class<?> source, String message) {
        if (Log.isLoggable(TAG, Log.WARN))  android.util.Log.w(TAG, String.format("[%s]: %s", source.getSimpleName(), message));
    }


}
