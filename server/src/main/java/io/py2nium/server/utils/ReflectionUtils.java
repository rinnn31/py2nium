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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import io.py2nium.server.common.exceptions.Py2niumException;

public class ReflectionUtils {
    public static Method getMethod(@NonNull final Class<?> clazz,@NonNull final String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            String err =  String.format("Cannot retrieve %s method from class %s with params %s",methodName, clazz.getSimpleName(), Arrays.toString(parameterTypes));
            Logger.e(ReflectionUtils.class, err, e);
            throw new Py2niumException(err);
        }
    }

    public static Method getMethod(@NonNull final String className,@NonNull final String methodName, final Class<?>... parameterTypes) {
        return getMethod(getClass(className), methodName, parameterTypes);
    }

    public static Object invokeMethod(@NonNull final Method method, final Object object, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(object, args);
        } catch (Exception e) {
            String err = String.format("Failed to invoke method %s on object %s with params %s",
                    method.getName(), object, Arrays.toString(args));
            Logger.e(ReflectionUtils.class, err, e);
            throw new Py2niumException(err);
        }
    }



    public static Class<?> getClass(@NonNull final String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            String err =  String.format("Cannot find Class for name '%s'", className);
            Logger.e(ReflectionUtils.class, err,e);
            throw new Py2niumException(err);
        }
    }

    public static Object getField(@NonNull final Object instance,final String fieldName) {
        Class<?> clazz = instance.getClass();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException e) {
            String err = String.format("Cannot find field with name '%s' in class %s", fieldName, clazz.getName());
            Logger.e(ReflectionUtils.class, err, e);
            throw new Py2niumException(err);
        } catch (IllegalAccessException e) {
            String err = String.format("Cannot get value of field %s in object %s", fieldName, instance);
            Logger.e(ReflectionUtils.class, err,e);
            throw new Py2niumException(err);
        }
    }

    public static Object getField(@NonNull final Class<?> clazz,final String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (NoSuchFieldException e) {
            String err = String.format("Cannot find field with name '%s' in class %s", fieldName, clazz.getName());
            Logger.e(ReflectionUtils.class, err, e);
            throw new Py2niumException(err);
        } catch (IllegalAccessException e) {
            String err = String.format("Cannot get value of static field %s in class %s", fieldName, clazz.getName());
            Logger.e(ReflectionUtils.class, err,e);
            throw new Py2niumException(err);
        }
    }

}
