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

import java.util.List;
import java.util.Map;

import io.py2nium.server.common.annotation.RequiredField;
import io.py2nium.server.common.exceptions.InvalidInputException;
import io.py2nium.server.model.ElementMatcher;
import io.py2nium.server.utils.Attribute;

public class ByModel extends InputModel {
    @RequiredField
    public String locator;

    @RequiredField
    public Object value;

    public String selectorMatchType = ElementMatcher.MATCH_ALL;

    public Long timeout = 0L;

    public Boolean findInvisibleElement = false;

    @SuppressWarnings("unchecked")
    @Override
    public void validate() {
        super.validate();

        switch (locator) {
            case "xpath":
                if(!(value instanceof String)) {
                    throw new InvalidInputException("Xpath expression must be a string");
                }
                break;
            case "selector":
                if(! (value instanceof List)) { // Gson deserialize object value to Map
                    throw new InvalidInputException("Selector must be an list of objects");
                }
                List<Object> mappedValue = (List<Object>) value;
                for(int i = 0; i < mappedValue.size(); i++) {
                    if(!(mappedValue.get(i) instanceof Map)) {
                        throw new InvalidInputException("Selector must be an list of objects");
                    }

                    Map<String, Object> attributeSelector = (Map<String, Object>) mappedValue.get(i);
                    if(!attributeSelector.containsKey("attribute") || !attributeSelector.containsKey("value") || !attributeSelector.containsKey("strategy")) {
                        throw new InvalidInputException(String.format("Attribute selector at index %d must contain attribute, strategy and value", i));
                    }

                    String strategy = (String) attributeSelector.get("strategy");
                    if(!ElementMatcher.EQUALS.equals(strategy) && !ElementMatcher.CONTAINS.equals(strategy)
                            && !ElementMatcher.STARTS_WITH.equals(strategy) && !ElementMatcher.ENDS_WITH.equals(strategy)
                            && !ElementMatcher.PATTERN.equals(strategy)) {
                        throw new InvalidInputException(String.format("Invalid strategy at object index %d", i));
                    }

                }
                break;
            default:
                throw new InvalidInputException("Invalid locator");
        }

        if(!selectorMatchType.equals(ElementMatcher.MATCH_ALL) && !selectorMatchType.equals(ElementMatcher.MATCH_ANY)) {
            throw new InvalidInputException("Invalid selectorMatchType");
        }

        if(timeout < 0) {
            throw new InvalidInputException("Timeout value cannot be negative");
        }

    }

    @SuppressWarnings("unchecked")
    public ElementMatcher getMatcher() {

        ElementMatcher matcher = new ElementMatcher(selectorMatchType);
        List<Object> listMatcher = (List<Object>) value;

        for(Object obj : listMatcher) {
            Map<String, Object> map = (Map<String, Object>) obj;
            String attributeStr = (String) map.get("attribute");
            Object value = (String) map.get("value");
            String strategy = (String) map.get("strategy");
            if(strategy == null) {
                strategy = ElementMatcher.EQUALS;
            }

            Attribute attribute = Attribute.fromString(attributeStr);
            if(attribute!= null) {
                matcher.build(attribute, value, strategy);
            } else {
                throw new InvalidInputException("Invalid attribute: " + attributeStr);
            }

        }
        return matcher;

    }
}
