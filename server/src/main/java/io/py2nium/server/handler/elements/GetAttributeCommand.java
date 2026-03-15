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

import io.netty.handler.codec.http.HttpMethod;
import io.py2nium.server.common.exceptions.InvalidInputException;
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;
import io.py2nium.server.model.AndroidDevice;
import io.py2nium.server.model.AndroidElement;
import io.py2nium.server.utils.Attribute;

public class GetAttributeCommand extends CommandHandler {
    public GetAttributeCommand(String uri, HttpMethod method) {
        super(uri, method);
    }
    @Override
    protected Py2niumResponse handle(HttpReq request) {
        String elementId = request.getExtraData(":element");
        String attributeName = request.getExtraData(":attribute");

        if(elementId == null || attributeName == null) {
            throw new InvalidInputException("Element id and attribute name are required");
        }
        Attribute attribute = Attribute.fromString(attributeName);
        if(attribute == null) throw new InvalidInputException(String.format("Can not find attribute with name '%s'", attributeName));

        AndroidElement element = AndroidDevice.getInstance().getElementsCache().getElement(elementId);

        return new Py2niumResponse(Py2niumResponse.SUCCESS, element.getAttributeValue(attribute), null);
    }
}
