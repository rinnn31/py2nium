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


package io.py2nium.server.model.api;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import io.py2nium.server.common.annotation.RequiredField;
import io.py2nium.server.common.exceptions.InvalidInputException;

public abstract class InputModel {

    private ArrayList<Field> getAllProperties() {
        ArrayList<Field> fields = new ArrayList<>(Arrays.asList(this.getClass().getDeclaredFields()));
        Class<?> superClass = this.getClass().getSuperclass();
        while(superClass != null && superClass != InputModel.class) {
            fields.addAll(Arrays.asList(superClass.getDeclaredFields()));
            superClass = superClass.getSuperclass();
        }

        return fields;
    }

    public void validate() {
        for(Field field : getAllProperties()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if(field.isAnnotationPresent(SerializedName.class) && !field.getAnnotation(SerializedName.class).value().trim().isEmpty()) {
                fieldName = field.getAnnotation(SerializedName.class).value();
            }
            Object fieldValue = null;
            try {
                fieldValue = field.get(this);
                if(fieldValue == null && field.isAnnotationPresent(RequiredField.class)) {
                    throw new InvalidInputException(String.format("%s: Field '%s' is required", this.getClass().getSimpleName(), fieldName));
                }
                if(fieldValue instanceof InputModel) {
                    ((InputModel) fieldValue).validate();
                }
                if(fieldValue instanceof InputModel[]) {
                    for(InputModel model : (InputModel[]) fieldValue) {
                        model.validate();
                    }
                }
            } catch (IllegalAccessException ignored) {
                continue;
            }
        }
    }
}
