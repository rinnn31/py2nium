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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ElementCollection implements Iterable<AndroidElement> {
    private final List<AndroidElement> collection = new ArrayList<>();

    public ElementCollection(List<AndroidElement> elementList) {
        collection.addAll(elementList);
    }

    public ElementCollection() {
        /* empty */
    }

    @Nullable
    public AndroidElement get(int index) {
        return index >= collection.size() ? null : collection.get(index);
    }

    public void add(AndroidElement element) {
        collection.add(element);
    }

    public void addAll(ElementCollection elements) {
        collection.addAll(elements.collection);
    }

    public int size() {
        return collection.size();
    }

    @NonNull
    @Override
    public Iterator<AndroidElement> iterator() {
        return new Iterator<AndroidElement>() {
            private int currentIndex = 0;
            @Override
            public boolean hasNext() {
                return currentIndex < collection.size();
            }

            @Override
            public AndroidElement next() {
                return collection.get(currentIndex++);
            }
        };
    }
}
