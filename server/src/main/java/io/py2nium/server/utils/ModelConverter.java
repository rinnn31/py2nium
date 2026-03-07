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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.py2nium.server.common.exceptions.InvalidInputException;
import io.py2nium.server.model.api.InputModel;

public class ModelConverter {
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static <T> T toModelAndValidate(String src, Class<T> clazz) {
        T modelHolder = toModel(src, clazz);
        validateInput(modelHolder);
        return modelHolder;
    }

    public static <T> T toModel(String src, Class<T> clazz) {
        if (src == null || src.isEmpty()) {
            src = "{}";
        }

        T instance = null;
        try {
            instance = gson.fromJson(src, clazz);
        } catch (Exception e) {
            throw new InvalidInputException("Invalid JSON format");
        }
        return instance;
    }

    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    private static void validateInput(Object input) {
        if (input == null) {
            return;
        }

        if(input.getClass().isArray()) {
            for(Object childInput : (Object[])input) {
                validateInput(childInput);
            }
        }
        if(input instanceof InputModel) {
            ((InputModel) input).validate();
        }
    }
}
