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


package io.py2nium.server.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

public class HttpReq {
    private final FullHttpRequest baseReq;
    private final Map<String, String> extraData = new HashMap<>();
    private final Map<String, String> queryParams = new HashMap<>();
    public HttpReq(FullHttpRequest request) {
        this.baseReq = request;
        Map<String, List<String>> queries = new QueryStringDecoder(request.uri()).parameters();
        for( String key : queries.keySet()) {
            queryParams.put(key, queries.get(key).get(0));
        }
    }

    public String getBody() {
        return baseReq.content().toString(io.netty.util.CharsetUtil.UTF_8);
    }

    public String getUri() {
        return baseReq.uri();
    }

    public HttpMethod getMethod() {
        return baseReq.method();
    }

    public String getExtraData(String key) {
        return extraData.get(key);
    }

    public void putExtraData(String key, String value) {
        extraData.put(key, value);
    }

    public String getParameter(String key, String defaultValue) {
        if(key == null || !queryParams.containsKey(key)) return defaultValue;
        return queryParams.get(key);
    }

    public String getParameter(String key) {
        return getParameter(key, null);
    }
}
