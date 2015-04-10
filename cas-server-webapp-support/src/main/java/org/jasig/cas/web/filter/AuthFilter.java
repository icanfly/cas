package org.jasig.cas.web.filter;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.util.UserUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author luopeng
 *         Created on 2015/4/10.
 */
public class AuthFilter implements Filter {

    private UserUtils userUtils;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if(StringUtils.isBlank(userUtils.getLoginPrincipal(req))){
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }else {
            chain.doFilter(request,response);
        }
    }

    @Override
    public void destroy() {

    }

    public void setUserUtils(UserUtils userUtils) {
        this.userUtils = userUtils;
    }
}
