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


package io.py2nium.server.handler.device;

import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;
import java.util.List;

import io.netty.handler.codec.http.HttpMethod;
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;
import io.py2nium.server.model.api.EventModel;
import io.py2nium.server.model.api.EventQueryModel;
import io.py2nium.server.utils.AxEventHelper;
import io.py2nium.server.utils.Logger;
import io.py2nium.server.utils.ModelConverter;

public class QueryEventCommand extends CommandHandler {
    public QueryEventCommand(String uri, HttpMethod... methods) {
        super(uri, methods);
    }

    @Override
    protected Py2niumResponse handle(HttpReq request) {
        EventQueryModel model = ModelConverter.toModelAndValidate(request.getBody(), EventQueryModel.class);
        if(model.deltaTime != null && model.count != null) {
            Logger.w(QueryEventCommand.class, "Both 'count' and 'deltaTime' fields are provided. 'deltaTime' field will be ignored");
        }

        List< AccessibilityEvent> events = null;
        if(model.count != null) {
            events = AxEventHelper.getInstance().getEventsByCount(model.count);
        } else {
            events = AxEventHelper.getInstance().getEventsByTime(model.deltaTime);
        }

        List<EventModel> result = new ArrayList<>();
        for(AccessibilityEvent event : events) {
            result.add(EventModel.fromAccessibilityEvent(event));
        }

        return new Py2niumResponse(Py2niumResponse.SUCCESS, result, null);
    }
}
