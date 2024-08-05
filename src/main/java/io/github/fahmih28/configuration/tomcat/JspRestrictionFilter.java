package io.github.fahmih28.configuration.tomcat;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JspRestrictionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (req.getDispatcherType() == DispatcherType.REQUEST) {
            res.sendError(403, "Access denied");
            return;
        }
        chain.doFilter(request, response);
    }
}
