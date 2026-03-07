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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StringUtils {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String trim(String str, char character) {
        if(str != null) {
            StringBuilder stringBuilder = new StringBuilder(str);
            while(stringBuilder.charAt(0) == character) {
                stringBuilder.deleteCharAt(0);
            }
            while(stringBuilder.charAt(stringBuilder.length() - 1) == character) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            return stringBuilder.toString();
        } else return null;
    }

    public static String charSequenceToString(CharSequence seq) {
        return seq == null ? "": seq.toString();
    }

    public static String stripInvalidXMLChars(String cs, char replacement) {
        StringBuilder ret = new StringBuilder();
        char ch;
        for (int i = 0; i < cs.length(); i++) {
            ch = cs.charAt(i);
            // http://www.w3.org/TR/xml11/#charsets
            if ((ch >= 0x1 && ch <= 0x8)
                    || (ch >= 0xB && ch <= 0xC)
                    || (ch >= 0xE && ch <= 0x1F)
                    || (ch >= 0x7F && ch <= 0x84)
                    || (ch >= 0x86 && ch <= 0x9F)
                    || (ch >= 0xFDD0 && ch <= 0xFDDF)) {
                ret.append(replacement);
            } else {
                ret.append(ch);
            }
        }
        return ret.toString();
    }

    public static byte[] inputStreamToByteArray(InputStream input) throws IOException {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;
            while ((length = input.read(buffer)) != EOF) {
                result.write(buffer, 0, length);
            }
            return result.toByteArray();
        }
    }
}
