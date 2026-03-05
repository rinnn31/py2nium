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

import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.py2nium.server.handler.CommandHandler;

public class HttpServerImpl {
    private HttpServerThread mServerThread = null;
    private final int PORT;
    private final String HOST;
    private final List<CommandHandler> mHandlersList = new ArrayList<>();
    public HttpServerImpl(String host, int port) {
        this.HOST = host;
        this.PORT = port;
    }

    public int getPort() {
        return PORT;
    }

    public String getHost() {
        return HOST;
    }

    public void addHandler(CommandHandler handler) {
        mHandlersList.add(handler);
    }

    public void startServer() {
        if(mServerThread != null) {
            throw new IllegalStateException("Server is already running");
        }
        mServerThread = new HttpServerThread();
        mServerThread.start();
    }

    public void stopServer() {
        if(mServerThread == null) {
            return;
        }
        mServerThread.interrupt();
        mServerThread = null;
    }

    public boolean isAlive() {
        return mServerThread.isServerAlive();
    }

    private class HttpServerThread extends Thread {
        private Looper mLooper;

        private Thread mRunnerThread;

        @Override
        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();

            mRunnerThread = new Thread(() -> {
                EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                EventLoopGroup workerGroup = new NioEventLoopGroup(0);
                try {
                    // Create the ServerBootstrap
                    ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, 1024)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.SO_KEEPALIVE, true)
                            .option(ChannelOption.SO_REUSEADDR, true)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast("codec", new HttpServerCodec());
                                    ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
                                    ch.pipeline().addLast("io/py2nium/server/handler", new HttpHandlerImpl(mHandlersList));
                                }
                            });

                    // Bind and start the server

                    serverBootstrap.bind(HOST, PORT).sync().channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    // Shut down the event loop groups
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });

            mRunnerThread.start();
            Looper.loop();
        }

        @Override
        public void interrupt() {
            mRunnerThread.interrupt();
            if(mLooper != null) {
                mLooper.quit();
            }

            super.interrupt();
        }

        public boolean isServerAlive() {
            return mRunnerThread == null ||  mRunnerThread.isAlive();
        }

    }
}
