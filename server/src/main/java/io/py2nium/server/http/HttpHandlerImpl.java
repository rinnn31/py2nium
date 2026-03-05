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


import android.net.Uri;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.py2nium.server.common.exceptions.UnknowPathException;
import io.py2nium.server.handler.CommandHandler;
import io.py2nium.server.utils.Logger;
import io.py2nium.server.utils.StringUtils;

public class HttpHandlerImpl extends ChannelInboundHandlerAdapter {
    private static final String API_PREFIX = "/py2nium/hub/";

    private final List<CommandHandler> mHandlersList;
    public HttpHandlerImpl(List<CommandHandler> handlersList) {
        this.mHandlersList = handlersList;
    }

    public CommandHandler getHandler(String path, HttpMethod method) {
        for (CommandHandler handler : mHandlersList) {
            if (handler.isMethodSupported(method) && isPathMatch(path, handler.getMappedPath())) {
                return handler;
            }
        }
        return null;
    }

    private void extractUrlParams(String actualPath, String mappedPath, HttpReq req) {
        actualPath = StringUtils.trim(actualPath, '/');
        mappedPath = StringUtils.trim(mappedPath, '/');
        String[] actualUriParts = actualPath.split("/");
        String[] mappedUriParts = mappedPath.split("/" );

        if(actualUriParts.length != mappedUriParts.length) {
            return;
        }
        for(int i = 0; i<actualUriParts.length; i++) {
            if(mappedUriParts[i].startsWith(":")) {
                req.putExtraData(mappedUriParts[i], actualUriParts[i]);
            }
        }
    }

    public boolean isPathMatch(String actualPath, String mappedUri) {
        actualPath = StringUtils.trim(actualPath, '/');
        mappedUri = StringUtils.trim(mappedUri, '/');
        String[] actualPathParts = actualPath.split("/");
        String[] mappedPathParts = mappedUri.split("/");

        if(actualPathParts.length != mappedPathParts.length) {
            return false;
        }
        for(int i = 0; i<mappedPathParts.length; i++) {
            if(mappedPathParts[i].startsWith(":")) {
                continue;
            }
            if(!mappedPathParts[i].equals(actualPathParts[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);

            String uri = request.uri();
            String path = Uri.parse(uri).getPath();
            Logger.i(this.getClass(), String.format("Handling request: %s -> %s", request.method().name(), uri));

            Py2niumResponse py2Response = null;
            if(StringUtils.isNullOrEmpty(path) || !path.startsWith(API_PREFIX)) {
                py2Response = new Py2niumResponse(Py2niumResponse.PREPROCESSING_ERROR, null, new UnknowPathException("Path must be started with " + API_PREFIX));
            } else {
                path = path.substring(API_PREFIX.length());
                CommandHandler handler = getHandler(path, request.method());
                if(handler != null) {
                    HttpReq req = new HttpReq(request);
                    extractUrlParams(path, handler.getMappedPath(), req);
                    py2Response = handler.safeHandle(req);
                } else {
                    Logger.i(this.getClass(), String.format("Cannot find handler for query: %s -> %s", request.method().name(), uri));
                    py2Response = new Py2niumResponse(Py2niumResponse.PREPROCESSING_ERROR, null, new UnknowPathException("Cannot find handler for path:" + uri));
                }
            }

            py2Response.writeTo(response);
            ctx.write(response);

        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }


}
