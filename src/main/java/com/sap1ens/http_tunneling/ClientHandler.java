package com.sap1ens.http_tunneling;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Process response from target server, transfer it to the {@link Tunnel}
 *
 * @author sap1ens
 */
public class ClientHandler extends SimpleChannelUpstreamHandler {

    final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private boolean readingChunks;

    private HTTPObject httpObject = new HTTPObject();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!readingChunks) {
            HttpResponse response = (HttpResponse) e.getMessage();

            logger.info("response status: {}", response.getStatus());

            logger.info("--- response headers ---");
            for (Map.Entry<String, String> h: response.getHeaders()) {
                logger.info("{} = {}", h.getKey(), h.getValue());

                httpObject.getHeaders().put(h.getKey(), h.getValue());
            }

            httpObject.getContent().setLength(0);

            if (response.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer content = response.getContent();
                if (content.readable()) {
                    httpObject.getContent().append(content.toString(CharsetUtil.UTF_8));
                }
                processResponse(e);
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                processResponse(e);
            } else {
                httpObject.getContent().append(chunk.getContent().toString(CharsetUtil.UTF_8));
            }
        }
    }

    private void processResponse(MessageEvent e) throws IOException, URISyntaxException {
        logger.info("--- response content ---");
        logger.info("{}", httpObject.getContent().toString() + "\r\n");

        Tunnel.getInstance().writeOutcoming(httpObject);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        e.getCause().printStackTrace();
        e.getChannel().close();
    }
}