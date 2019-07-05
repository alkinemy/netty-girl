package al.netty.apiserver.domain.server.handler;

import al.netty.apiserver.domain.server.service.ApiRequest;
import al.netty.apiserver.domain.server.service.ServiceDispatcher;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ApiRequestParser extends SimpleChannelInboundHandler<FullHttpMessage> {

    private static final Logger logger = LogManager.getLogger(ApiRequestParser.class);

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private static final Set<String> usingHeader = new HashSet<>();

    private HttpRequest request;
    private JsonObject apiResult;

    private HttpPostRequestDecoder decoder;

    private Map<String, String> requestData = new HashMap<>();

    static {
        usingHeader.add("token");
        usingHeader.add("email");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpMessage msg) throws Exception {
        if (msg instanceof HttpRequest) {
            this.request = (HttpRequest) msg;

            if (HttpHeaders.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            HttpHeaders headers = request.headers();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers) {
                    String key = header.getKey();
                    if (usingHeader.contains(key)) {
                        requestData.put(key, header.getValue());
                    }
                }
            }

            requestData.put("REQUEST_URI", request.getUri());
            requestData.put("REQUEST_METHOD", request.getMethod().name());
        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();

            if (msg instanceof LastHttpContent) {
                logger.debug("LastHttpContent message receive!!: " + request.getUri());
                LastHttpContent trailer = (LastHttpContent) msg;
                readPostData();
                ApiRequest service = ServiceDispatcher.dispatch(requestData);

                try {
                    service.executeService();
                    apiResult = service.getApiResult();
                } finally {
                    requestData.clear();
                }

                if (!writeResponse(trailer, ctx)) {
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                            .addListener(ChannelFutureListener.CLOSE);
                }
                reset();
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.info("요청 처리 완료");
        ctx.flush();
    }

    private boolean writeResponse(LastHttpContent trailer, ChannelHandlerContext ctx) {
        return false;
    }

    private void reset() {

    }

    private void send100Continue(ChannelHandlerContext ctx) {

    }

    private void readPostData() {
        try {
            decoder = new HttpPostRequestDecoder(factory, request);
            for(InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (InterfaceHttpData.HttpDataType.Attribute == data.getHttpDataType()) {
                    try {
                        Attribute attribute = (Attribute) data;
                        requestData.put(attribute.getName(), attribute.getValue());
                    } catch (IOException e) {
                        logger.error("BODY Attribute: " + data.getHttpDataType().name(), e);
                        return;
                    }
                } else {
                    logger.info("BODY data: " + data.getHttpDataType().name() + ": " + data);
                }
            }
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {
            e.printStackTrace();
        } finally {
            if (decoder != null) {
                decoder.destroy();
            }
        }
    }

}
