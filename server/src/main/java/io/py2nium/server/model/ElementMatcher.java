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

import java.util.HashMap;
import java.util.regex.Pattern;

import io.py2nium.server.utils.Attribute;


public class ElementMatcher {
    public static final Attribute[] SUPPORTED_ATTRIBUTE = new Attribute[] {
            Attribute.INDEX, Attribute.CLASS, Attribute.PACKAGE, Attribute.RESOURCE_ID,
            Attribute.CONTENT_DESC, Attribute.TEXT, Attribute.HINT, Attribute.PASSWORD,
            Attribute.CHECKABLE, Attribute.CLICKABLE, Attribute.LONG_CLICKABLE, Attribute.ENABLED,
            Attribute.CHECKED, Attribute.DISPLAYED, Attribute.FOCUSABLE, Attribute.FOCUSED,
            Attribute.SCROLLABLE, Attribute.SELECTED
    };

    public static final String MATCH_ALL = "match_all";
    public static final String MATCH_ANY = "match_any";
    public static final String EQUALS = "equals";
    public static final String STARTS_WITH = "starts-with";
    public static final String ENDS_WITH = "ends-with";
    public static final String CONTAINS = "contains";
    public static final String PATTERN = "pattern";

    private final HashMap<Attribute, ValueMatcher> attributeMatchers = new HashMap<>();

    private final String matchType;
    public ElementMatcher(String matchType) {
        this.matchType = matchType;
    }

    public ElementMatcher build(Attribute attribute, Object value) {
        return build(attribute, value, "equals");
    }

    public ElementMatcher build(Attribute attribute, Object value, String matcherStrategy) {
        attributeMatchers.put(attribute, new ValueMatcher(value, matcherStrategy));
        return this;
    }

    public boolean isMatched(@NonNull AndroidElement element) {
        boolean val = false;
        for(Attribute attribute : attributeMatchers.keySet()) {
            ValueMatcher valueMatcher = attributeMatchers.get(attribute);
            Object elementValue = element.getAttributeValue(attribute);
            val = valueMatcher.match(elementValue) || val;
            if(matchType.equals(MATCH_ALL) && !val) {
                return false;
            } else if(matchType.equals(MATCH_ANY) && val) {
                return true;
            }
        }
        return val;
    }

    private static class ValueMatcher {
        String matcherStrategy;
        Object value;
        public ValueMatcher(Object value, String matcherStrategy) {
            this.matcherStrategy = matcherStrategy;
            this.value = value;
        }
        public boolean match(Object elementValue) {
            if(matcherStrategy.equals(EQUALS)) {
                return elementValue.equals(value);
            } else {
                if(!(value instanceof String)) {
                    return false;
                }
                String valueStr = (String)elementValue;
                switch (matcherStrategy) {
                    case STARTS_WITH:
                        return valueStr.startsWith((String) value);
                    case ENDS_WITH:
                        return valueStr.endsWith((String)value);
                    case CONTAINS:
                        return valueStr.contains((String)value);
                    case PATTERN:
                        return Pattern.matches((String)value, valueStr);
                    default:
                        return false;
                }
            }
        }
    }

}

