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


package io.py2nium.server.handler;

import java.util.Arrays;

import io.netty.handler.codec.http.HttpMethod;
import io.py2nium.server.common.exceptions.InvalidInputException;
import io.py2nium.server.common.exceptions.Py2niumException;
import io.py2nium.server.http.HttpReq;
import io.py2nium.server.http.Py2niumResponse;

public abstract class CommandHandler {

    protected String mappedUri;
    protected HttpMethod[] supportedMethods;

    public CommandHandler(String uri, HttpMethod... methods) {
        this.mappedUri = uri;
        this.supportedMethods = methods;
    }


    public Py2niumResponse safeHandle(HttpReq request) {
        try
        {
            if (!isMethodSupported(request.getMethod())) {
                return new Py2niumResponse(Py2niumResponse.PREPROCESSING_ERROR, null,
                        new Py2niumException(String.format("This api doesn't support %s method. Use %s instead.",
                                        request.getMethod().name(), Arrays.toString(supportedMethods))));
            }
            return handle(request);
        } catch (InvalidInputException e) {
            return new Py2niumResponse(Py2niumResponse.PREPROCESSING_ERROR, null, e);
        } catch (Exception e) {
            e.printStackTrace();
            return new Py2niumResponse(Py2niumResponse.INTERNAL_ERROR, null, e);
        }
    }

    protected abstract Py2niumResponse handle(HttpReq request);

    public String getMappedPath() {
        return mappedUri;
    }

    public boolean isMethodSupported(HttpMethod method) {
        return Arrays.asList(supportedMethods).contains(method);
    }
}
