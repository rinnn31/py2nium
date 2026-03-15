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


package io.py2nium.server.handler.elements;

import java.util.ArrayList;
import java.util.List;

import io.netty.handler.codec.http.HttpMethod;
import io.py2nium.server.common.exceptions.InvalidInputException;
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;
import io.py2nium.server.model.AndroidDevice;
import io.py2nium.server.model.AndroidElement;
import io.py2nium.server.model.ElementCollection;
import io.py2nium.server.model.ElementsCache;
import io.py2nium.server.model.api.ElementModel;

public class ElementChildrenCommand extends CommandHandler {
    public ElementChildrenCommand(String uri, HttpMethod... methods) {
        super(uri, methods);
    }

    @Override
    protected Py2niumResponse handle(HttpReq request) {
        String elementId = request.getExtraData(":element");
        String query = request.getParameter("query", "get_all");

        ElementsCache elementsCache = AndroidDevice.getInstance().getElementsCache();
        AndroidElement element = elementsCache.getElement(elementId);
        switch (query) {
            case "get_all":
                ElementCollection collection = element.getChildren();
                List<ElementModel> result = new ArrayList<>();
                for(int i =0 ;i<collection.size();i++) {
                    AndroidElement child = collection.get(i);

                    String cachedElementId = elementsCache.putElement(child);
                    result.add(new ElementModel(cachedElementId));
                }
                return new Py2niumResponse(Py2niumResponse.SUCCESS, result, null);
            case "get":
                int index = Integer.parseInt(request.getParameter("index", "0"));
                AndroidElement child = element.getChild(index);
                String cachedElementId = elementsCache.putElement(child);

                return new Py2niumResponse(Py2niumResponse.SUCCESS, new ElementModel[]{new ElementModel(cachedElementId)}, null);
            case "count":
                return new Py2niumResponse(Py2niumResponse.SUCCESS, element.getChildCount(), null);
            default:
                throw new InvalidInputException("Invalid query");
        }
    }
}
