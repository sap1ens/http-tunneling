package com.sap1ens.http_tunneling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class Server {

    private final int port;

    public Server() throws IOException {
        this.port = Configuration.getInstance().getServerPort();
    }

    public void run() {
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ServerPipelineFactory());
        bootstrap.bind(new InetSocketAddress(port));
    }

    public static void main(String[] args) throws IOException {
        new Server().run();
    }
}