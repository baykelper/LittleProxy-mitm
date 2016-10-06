package org.littleshoot.proxy.mitm;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.log4j.xml.DOMConfigurator;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(final String... args) {
        File log4jConfigurationFile = new File(
                "src/test/resources/log4j.xml");
        if (log4jConfigurationFile.exists()) {
            DOMConfigurator.configureAndWatch(
                    log4jConfigurationFile.getAbsolutePath(), 15);
        }
        try {
            final int port = 9090;

            System.out.println("About to start server on port: " + port);
            HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer
                    .bootstrapFromFile("./littleproxy.properties")
                    .withPort(port).withAllowLocalOnly(false);

            HostNameMitmManager mgr = new HostNameMitmManager();
            bootstrap.withManInTheMiddle(mgr);

            bootstrap.withFiltersSource(filtersSource);

            System.out.println("About to start...");
            bootstrap.start();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private static HttpFiltersSource filtersSource = new HttpFiltersSourceAdapter() {


        public HttpFilters filterRequest(HttpRequest originalRequest) {
            return new HttpFiltersAdapter(originalRequest, null);
        }

        @Override
        public HttpFilters filterRequest(HttpRequest originalRequest,
                                         ChannelHandlerContext ctx) {

            // The connect request must bypass the filter! Otherwise the
            // handshake will fail.
            //
            if (ProxyUtils.isCONNECT(originalRequest)) {
                log.debug(" connect: {} {}",originalRequest.getMethod().name(), originalRequest.getUri());
                return new HttpFiltersAdapter(originalRequest);
            }

            return new HttpFiltersAdapter(originalRequest) {

                /**
                 * This filter delivers special responses if connection
                 * limited
                 */
                @Override
                public HttpResponse clientToProxyRequest(
                        HttpObject httpObject) {
                    log.debug("  foo: {}",httpObject.getDecoderResult().toString());
                    return super.clientToProxyRequest(httpObject);
                }

                /**
                 * This proxy expect aggregated chunks only, with https too
                 */
                @Override
                public HttpObject proxyToClientResponse(
                        HttpObject httpObject) {
                    if (httpObject instanceof FullHttpResponse) {
                        log.debug(" returning: httpObject {}", (FullHttpResponse)httpObject);
                        return super.proxyToClientResponse(httpObject);
                      } else {
                        throw new IllegalStateException(
                                "Response is not been aggregated");
                    }
                }
            };
        }

        /** This proxy must aggregate chunks */
        @Override
        public int getMaximumResponseBufferSizeInBytes() {
            return 10 * 1024 * 1024;
        }


        //   @Override
        //   public int getMaximumResponseBufferSizeInBytes() {
        //       return 0;
        //   }
    };
}
