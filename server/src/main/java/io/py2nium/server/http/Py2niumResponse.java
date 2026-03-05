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

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import io.netty.handler.codec.http.FullHttpResponse;
import io.py2nium.server.utils.ModelConverter;

public class Py2niumResponse {
    public static final int SUCCESS = 0;
    public static final int PREPROCESSING_ERROR = -1;
    public static final int INTERNAL_ERROR = -2;
    
    private final Integer status;
    private final Object data;
    private final Error error;

    public Py2niumResponse(int status, Object data, Throwable thr) {
        this.status = status;
        this.data = data;
        if(thr != null) {
            this.error = new Error();
            this.error.errorType = thr.getClass().getSimpleName();
            this.error.errorMessage = thr.getMessage();
        } else {
            error = null;
        }
    }

    public Py2niumResponse(Object data) {
        this(SUCCESS, data, null);
    }

    public Integer getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

    public Error getError() {
        return error;
    }

    public static class Error {
        @SerializedName("error_message")
        public String errorMessage;
        @SerializedName("error_type")
        public String errorType;
    }

    public void writeTo(@NonNull FullHttpResponse response) {
        response.headers().set("Content-Type", "application/json");
        String json = ModelConverter.toJson(this);
        response.content().writeBytes(json.getBytes());
        response.headers().set("Content-Length", response.content().readableBytes());
    }
}
