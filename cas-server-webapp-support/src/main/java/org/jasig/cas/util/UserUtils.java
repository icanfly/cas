package org.jasig.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * utils for user management
 * @author luopeng
 *         Created on 2015/4/10.
 */
public class UserUtils {

    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Resource
    private CentralAuthenticationService centralAuthenticationService;

    private Logger logger = LoggerFactory.getLogger(UserUtils.class);

    public String getLoginPrincipal(HttpServletRequest request){
        String tgc = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        if(StringUtils.isBlank(tgc)){
            logger.error("tgc is null");
            return null;
        }

        TicketGrantingTicket ticket = null;
        try {
            ticket = centralAuthenticationService.getTicket(tgc,TicketGrantingTicket.class);
            if(ticket != null && !ticket.isExpired()){
                Authentication auth = ticket.getAuthentication();
                return auth.getPrincipal().getId();
            }

            return null;

        } catch (InvalidTicketException e) {
            logger.error("tgt invalid");
            return null;
        }
    }

    public void setTicketGrantingTicketCookieGenerator(CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setCentralAuthenticationService(CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
