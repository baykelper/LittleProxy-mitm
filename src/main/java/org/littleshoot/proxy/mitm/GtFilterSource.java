package org.littleshoot.proxy.mitm;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gtbradley on 10/6/2016.
 */
public class GtFilterSource extends HttpFiltersSourceAdapter {
    private static final Logger log = LoggerFactory.getLogger(GtFilterSource.class);


    public HttpFilters filterRequest(HttpRequest originalRequest) {
        log.warn("****  one param init called *****");
        return new HttpFiltersAdapter(originalRequest, null);
    }

    @Override
    public HttpFilters filterRequest(HttpRequest originalRequest,
                                     ChannelHandlerContext ctx) {

        // The connect request must bypass the filter! Otherwise the
        // handshake will fail.
        if (ProxyUtils.isCONNECT(originalRequest)) {
            log.debug(" connect: {} {}"
                    , originalRequest.getMethod().name(), originalRequest.getUri());
            return new HttpFiltersAdapter(originalRequest);
        }

        return new GtHttpFilters(originalRequest);
    }

    /**
     * This proxy must aggregate chunks
     */
    @Override
    public int getMaximumResponseBufferSizeInBytes() {
        return 100 * 1024 * 1024;
    }


    //   @Override
    //   public int getMaximumResponseBufferSizeInBytes() {
    //       return 0;
    //   }
}
