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
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;
import io.py2nium.server.model.AndroidDevice;
import io.py2nium.server.model.AndroidElement;
import io.py2nium.server.model.ElementCollection;
import io.py2nium.server.model.ElementsCache;
import io.py2nium.server.model.api.ByModel;
import io.py2nium.server.model.api.ElementModel;
import io.py2nium.server.utils.ElementExtractor;
import io.py2nium.server.utils.ModelConverter;

public class FindElementCommand extends CommandHandler {
    public FindElementCommand(String uri,HttpMethod method) {
        super(uri, method);
    }

    @Override
    protected Py2niumResponse handle(HttpReq request) {
        ByModel model = ModelConverter.toModelAndValidate(request.getBody(), ByModel.class);

        ElementsCache elementsCache = AndroidDevice.getInstance().getElementsCache();
        ElementCollection collection;
        AndroidElement root = null;
        if(request.getExtraData(":element") != null) {
            root = elementsCache.getElement(request.getExtraData(":element"));
        }

        List<ElementModel> result = new ArrayList<>();
        collection = ElementExtractor.find(root, model, true, model.timeout * 1000);
        if(collection.size() != 0) {
            String cachedElementId = elementsCache.putElement(collection.get(0));
            result.add(new ElementModel(cachedElementId));
        }

        return new Py2niumResponse(Py2niumResponse.SUCCESS, result, null);
    }
}
