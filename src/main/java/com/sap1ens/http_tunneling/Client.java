package com.sap1ens.http_tunneling;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make request to the target server.
 * Use fake SSL.
 *
 * @author sap1ens
 */
public class Client {

    final Logger logger = LoggerFactory.getLogger(Client.class);

    private final URI uri;

    private HTTPObject httpObject = new HTTPObject();

    public Client(URI uri, HTTPObject httpObject) {
        this.uri = uri;
        this.httpObject = httpObject;
    }

    public void run() {
        String scheme = uri.getScheme() == null? "http" : uri.getScheme();
        final String host = uri.getHost() == null? "localhost" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if (scheme.equalsIgnoreCase("http")) {
                port = 80;
            } else if (scheme.equalsIgnoreCase("https")) {
                port = 443;
            }
        }

        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            System.err.println("Only HTTP(S) is supported.");
            return;
        }

        boolean ssl = scheme.equalsIgnoreCase("https");

        final ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ClientPipelineFactory(ssl));

        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.getChannel();
                if (!future.isSuccess()) {
                    future.getCause().printStackTrace();
                    return;
                }

                String requestURL = uri.getRawPath() + httpObject.getUri();
                logger.info("request URL: {}", requestURL);

                HttpRequest request = new DefaultHttpRequest(
                        HttpVersion.HTTP_1_0, HttpMethod.POST, requestURL);

                for(Map.Entry<String, String> entry : httpObject.getHeaders().entrySet()) {
                    if(entry.getKey().equals("Host")) {
                        request.setHeader(HttpHeaders.Names.HOST, host);
                        continue;
                    }

                    request.setHeader(entry.getKey(), entry.getValue());
                }

                ChannelBuffer cb = ChannelBuffers.copiedBuffer(httpObject.getContent(), Charset.defaultCharset());

                request.setContent(cb);

                channel.write(request);
            }
        });
    }
}
