package io.github.fahmih28.configuration.tomcat;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * this filter is used by tomcat server to redirect request using directory name to the file index.jsp/jspx resides in it
 */
public class JspIndexFilter implements Filter {

    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String redirect = filterConfig.getInitParameter(httpRequest.getRequestURI());

        if(redirect == null){
            chain.doFilter(request, response);
            return;
        }

        filterConfig.getServletContext()
                .getRequestDispatcher(redirect).forward(request, response);
    }
}
