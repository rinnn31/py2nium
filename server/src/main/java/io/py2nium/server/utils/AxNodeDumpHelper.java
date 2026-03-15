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
import android.util.SparseArray;
import android.util.Xml;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import io.py2nium.server.core.UiAutomatorWrapper;
import io.py2nium.server.model.AndroidElement;
import io.py2nium.server.model.ElementCollection;

public class AxNodeDumpHelper {
    private static final String NAMESPACE = "";
    private static final String ENCODING = "UTF-8";
    private static final char DEFAULT_INVALID_CHAR_REPLACEMENT = '?';
    public static final String MAP_INDEX_STR = "map-index";

    @Nullable
    private final AccessibilityNodeInfo mRoot;
    private final boolean mIsIndexing;
    private final boolean mDumpInvisibleNode;
    private final SparseArray<AndroidElement> mNodeMapping = new SparseArray<>();

    public AxNodeDumpHelper(@Nullable AccessibilityNodeInfo root, boolean isIndexing, boolean dumpInvisibleNode) {
        this.mRoot = root;
        this.mIsIndexing = isIndexing;
        mDumpInvisibleNode = dumpInvisibleNode;
    }

    public OutputStream toStream() {
        UiAutomation uiAutomation = UiAutomatorWrapper.getUiAutomation();
        ElementCollection elementList = new ElementCollection();
        if(mRoot == null) {
            List<AccessibilityNodeInfo> roots = AxWindowHelper.getWindowRoots();
            for(int i = 0;i< roots.size();i++) {
                AccessibilityNodeInfo root = roots.get(i);
                if(root != null) {
                    elementList.add(new AndroidElement(root, i));
                }
            }
        } else elementList.add(new AndroidElement(mRoot, 0));

        ByteArrayOutputStream buildStream = new ByteArrayOutputStream();
        try {
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.setOutput(buildStream, ENCODING);

            serializer.startDocument(ENCODING, true);
            serializer.startTag(NAMESPACE, "hierarchy");
            serializer.attribute(NAMESPACE, "rotation", Integer.toString(UiAutomatorWrapper.getUiDevice().getDisplayRotation()));

            mNodeMapping.clear();

            for (int i = 0;i< elementList.size();i++) {
                buildChild(serializer, elementList.get(i));
            }

            serializer.endTag(NAMESPACE, "hierarchy");
            serializer.endDocument();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buildStream;
    }

    public AndroidElement getElement(int key) {
        return mNodeMapping.get(key);
    }

    private void buildChild(XmlSerializer serializer, AndroidElement element) throws IOException {
        serializer.startTag("", (String)element.getAttributeValue(Attribute.CLASS));
        if(mIsIndexing) {
            int ind = mNodeMapping.size();
            serializer.attribute(NAMESPACE, MAP_INDEX_STR, String.valueOf(ind));
            mNodeMapping.append(ind, element);
        }
        for(Attribute attr : AndroidElement.EXPOSED_ATTRIBUTES) {
            String value = element.getAttributeValue(attr) != null ?
                    StringUtils.stripInvalidXMLChars(element.getAttributeValue(attr).toString(), DEFAULT_INVALID_CHAR_REPLACEMENT) :
                    "";
            serializer.attribute(NAMESPACE, attr.toString(), value);
        }


        for(AndroidElement child : element.getChildren()) {
            if((boolean) child.getAttributeValue(Attribute.DISPLAYED) || mDumpInvisibleNode)
                buildChild(serializer, child);
        }
        serializer.endTag("", (String)element.getAttributeValue(Attribute.CLASS));
    }

    public String toXMLStr() throws IOException {

        try(ByteArrayOutputStream buildStream = (ByteArrayOutputStream) toStream()) {
            return buildStream.toString("UTF-8");
        }
    }
}
