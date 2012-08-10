package com.sap1ens.http_tunneling;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Transfer request from Server to Client via {@link HTTPObject}
 * Take response from Client and write it out.
 *
 * @author sap1ens
 */
public class Tunnel {

    private static Tunnel instance;

    private MessageEvent responseEvent;

    public static synchronized Tunnel getInstance() {
        if(instance == null) {
            instance = new Tunnel();
        }
        return instance;
    }

    public void readIncoming(HTTPObject httpObject) throws IOException, URISyntaxException {
        URI uri = new URI(Configuration.getInstance().getClientUri());
        new Client(uri, httpObject).run();
    }

    public void writeOutcoming(HTTPObject httpObject) throws IOException, URISyntaxException {
        HttpRequest request = (HttpRequest) responseEvent.getMessage();

        boolean keepAlive = isKeepAlive(request);

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);

        for(Map.Entry<String, String> entry : httpObject.getHeaders().entrySet()) {
            if(entry.getKey().equalsIgnoreCase("host") || entry.getKey().equalsIgnoreCase("transfer-encoding")) continue;

            response.setHeader(entry.getKey(), entry.getValue());
        }

        response.setContent(ChannelBuffers.copiedBuffer(httpObject.getContent(), CharsetUtil.UTF_8));

        ChannelFuture future = responseEvent.getChannel().write(response);

        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void setResponseEvent(MessageEvent responseEvent) {
        this.responseEvent = responseEvent;
    }
}
