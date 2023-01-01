package net.sothatsit.royalurserver.network;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Adds a "remote_addr" header requests that represents
 * the IP address of the client that sent the request.
 *
 * @source https://stackoverflow.com/a/23590606/8524395
 * @author wf
 */
public class RemoteAddrFilter implements Filter {

    /**
     * The name of the header that is added to requests.
     */
    public static final String HEADER_NAME = "remote_addr";

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest))
            throw new IllegalArgumentException("Expected a request of type " + HttpServletRequest.class);

        HttpServletRequest req = (HttpServletRequest) request;
        HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(req);
        requestWrapper.addHeader(HEADER_NAME, request.getRemoteAddr());
        chain.doFilter(requestWrapper, response); // Goes to default servlet.
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
