package net.royalur.backend.network;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

/**
 * Allows additional headers to be added to a request.
 * <p>
 * See:
 * <ul>
 *   <li>https://stackoverflow.com/questions/2811769/adding-an-http-header-to-the-request-in-a-servlet-filter</li>
 *   <li>http://sandeepmore.com/blog/2010/06/12/modifying-http-headers-using-java/</li>
 *   <li>http://bijubnair.blogspot.de/2008/12/adding-header-information-to-existing.html</li>
 * </ul>
 *
 * @author wf
 */
public class HeaderMapRequestWrapper extends HttpServletRequestWrapper {

    /**
     * The headers that are added by this wrapper.
     */
    private final Map<String, String> headerMap = new LinkedHashMap<>();

    /**
     * Instantiate a wrapper to add headers to the given request.
     *
     * @param request The request to wrap.
     */
    public HeaderMapRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * Add a header to the request.
     *
     * @param name  The name of the header.
     * @param value The value of the header.
     */
    public void addHeader(String name, String value) {
        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        if (headerMap.containsKey(name))
            return headerMap.get(name);

        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        names.addAll(headerMap.keySet());
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> values = Collections.list(super.getHeaders(name));
        if (headerMap.containsKey(name)) {
            values.add(headerMap.get(name));
        }
        return Collections.enumeration(values);
    }
}
