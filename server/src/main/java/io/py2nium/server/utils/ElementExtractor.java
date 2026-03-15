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

import android.app.UiAutomation;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.Nullable;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Time;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.stream.StreamSource;

import io.py2nium.server.core.UiAutomatorWrapper;
import io.py2nium.server.model.AndroidElement;
import io.py2nium.server.model.ElementCollection;
import io.py2nium.server.model.ElementMatcher;
import io.py2nium.server.model.api.ByModel;

public class ElementExtractor {

    static final long FIND_ELEMENT_DELAY_TIME = 200;

    public static ElementCollection findElementsByXpath(@Nullable AndroidElement element, String expression, boolean singleMode, boolean findInvisibleElement) {
        AxNodeDumpHelper dumper = new AxNodeDumpHelper(element == null ? null : element.getNode(), true, findInvisibleElement);
        ElementCollection matchedElements = new ElementCollection();
        try {
            Processor saxonProcessor = new Processor(false);

            DocumentBuilder builder = saxonProcessor.newDocumentBuilder();
            String xml = dumper.toXMLStr();
            StringReader reader = new StringReader(xml);
            XdmNode doc = builder.build(new StreamSource(reader));
            reader.close();

            XPathSelector selector = saxonProcessor.newXPathCompiler().compile(expression).load();
            selector.setContextItem(doc);

            for(XdmItem item : selector.evaluate()) {
                if(!(item instanceof XdmNode)) continue;
                XdmNode node = ((XdmNode) item);
                if (node.getNodeKind() != XdmNodeKind.ELEMENT || node.attribute(AxNodeDumpHelper.MAP_INDEX_STR) == null) continue;

                AndroidElement curElement = dumper.getElement(Integer.parseInt(node.attribute(AxNodeDumpHelper.MAP_INDEX_STR)));
                if (!findInvisibleElement && curElement.getAttributeValue(Attribute.DISPLAYED).equals(false)) {
                    continue;
                }

                matchedElements.add(curElement);
                if (singleMode && matchedElements.size() == 1) break;
            }
        } catch (SaxonApiException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return matchedElements;
    }

    public static ElementCollection findElementsByMatcher(
            @Nullable AndroidElement element, ElementMatcher matcher, boolean singleMode, boolean findInvisibleElement) {

        ElementCollection matchedElements = new ElementCollection();

        UiAutomation uiAutomation = UiAutomatorWrapper.getUiAutomation();
        ElementCollection elementList = new ElementCollection();
        if(element == null) {
            AccessibilityNodeInfo activeRootNode = uiAutomation.getRootInActiveWindow();
            if(activeRootNode != null) {
                elementList.add(new AndroidElement(activeRootNode, 0));
            }
            List<AccessibilityWindowInfo> windows = uiAutomation.getWindows();
            for(int i = 0 ;i< windows.size();i++) {
                elementList.add(new AndroidElement(windows.get(i).getRoot(), i+1));
            }
        } else elementList.add(element);

        for(AndroidElement singleElement : elementList) {
            if(!findInvisibleElement && singleElement.getAttributeValue(Attribute.DISPLAYED).equals(false)) {
                continue;
            }
            if(matcher.isMatched(singleElement)) {
                matchedElements.add(singleElement);
            }
            if(singleMode && matchedElements.size() == 1) break;
            matchedElements.addAll(findElementsByMatcher(singleElement, matcher, singleMode, findInvisibleElement));
        }

        return matchedElements;
    }

    public static ElementCollection find(@Nullable AndroidElement rootElement, ByModel by, boolean singleMode, long timeout) {
        ElementCollection collection = new ElementCollection();
        long startTime = System.currentTimeMillis();
        do {
            switch (by.locator) {
                case "xpath":
                    collection.addAll(findElementsByXpath(rootElement, (String) by.value, singleMode, by.findInvisibleElement));
                    break;
                case "selector":
                    collection.addAll(findElementsByMatcher(rootElement, by.getMatcher(), singleMode, by.findInvisibleElement));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid locator.");
            }

            if(collection.size() != 0) break;
            SystemClock.sleep(FIND_ELEMENT_DELAY_TIME);
        } while(System.currentTimeMillis() - startTime < timeout);

        return collection;
    }
}
