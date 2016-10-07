package org.littleshoot.proxy.mitm;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gtbradley on 10/6/2016.
 */
class GtHttpFilters extends HttpFiltersAdapter {
    private static final Logger log = LoggerFactory.getLogger(GtHttpFilters.class);

    public GtHttpFilters(HttpRequest originalRequest) {
        super(originalRequest);
    }

    /**
     * This filter delivers special responses if connection
     * limited
     */
    @Override
    public HttpResponse clientToProxyRequest(
            HttpObject httpObject) {
        log.debug("  foo: {}", httpObject.getDecoderResult().toString());
        //   httpObject.headers.  method, uri,
        return super.clientToProxyRequest(httpObject);
    }

    /**
     * This proxy expect aggregated chunks only, with https too
     */
    @Override
    public HttpObject proxyToClientResponse(
            HttpObject httpObject) {
        if (httpObject instanceof FullHttpResponse) {
            //     log.debug(" returning: httpObject {}", (FullHttpResponse)httpObject);
            return super.proxyToClientResponse(httpObject);
        } else {
            throw new IllegalStateException(
                    "Response is not been aggregated");
        }
    }
}

