package io.lightflame.store;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.lightflame.context.FlameHttpContext;
import io.lightflame.functions.FlameHttpFunction;
import io.lightflame.store.HttpRouteRules.HttpRouteRule;
import io.lightflame.store.HttpRouteRules.RuleEnum;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;


/**
 * HttpBeanStore
 */
public class FlameHttpStore {

    static private Map<String, FlameHttpFunction> functionMap = new HashMap<>();

    private String prefix = "";

    public FlameHttpStore() {
    }

    public FlameHttpStore(String prefix) {
        this.prefix = prefix;
    }

    public FlameHttpContext runFunctionByRequest(FullHttpRequest request) throws Exception{
        FlameHttpFunction function = handler404();

        HttpRouteRule httpRule = HttpRouteRules.processRequest(request);
        if (httpRule != null) {
            function = functionMap.get(httpRule.getId());
        }

        FlameHttpContext ctx = new FlameHttpContext(request, new FlameHttpUtils(), httpRule);
        return function.chain(ctx);
    }

    public FlameHttpStore addHeaderRyle(){
        return this;
    }

    public void httpGET(String url, FlameHttpFunction function){
        String key = UUID.randomUUID().toString();
        new HttpRouteRules().newRoute(key)
            .addRule(RuleEnum.PATH, this.prefix + url)
            .addRule(RuleEnum.METHOD, "GET")
            .set();
        functionMap.put(key, function);
    }


    public void httpPOST(String url, FlameHttpFunction function){
        String key = UUID.randomUUID().toString();
        new HttpRouteRules().newRoute(key)
            .addRule(RuleEnum.PATH, this.prefix + url)
            .addRule(RuleEnum.METHOD, "POST")
            .set();
        functionMap.put(key, function);
    }

    private FlameHttpFunction handler404() {
        return (ctx) -> {
            FullHttpResponse response = new DefaultFullHttpResponse(
                ctx.getRequest().protocolVersion(), 
                OK,
                Unpooled.wrappedBuffer("nothing here.. =(".getBytes()));
            response.headers()
                    .set(CONTENT_TYPE, TEXT_PLAIN)
                    .setInt(CONTENT_LENGTH, response.content().readableBytes());
            ctx.setResponse(response);
            return ctx;
        };
    }
    
}