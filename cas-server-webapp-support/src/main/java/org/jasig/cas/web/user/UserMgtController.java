package org.jasig.cas.web.user;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author luopeng
 *         Created on 2015/4/9.
 */
@Controller
@RequestMapping("/user")
public class UserMgtController {

    @Resource(name = "ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Resource
    private CentralAuthenticationService centralAuthenticationService;

    @RequestMapping("/modify_password")
    public String test(HttpServletRequest request) throws InvalidTicketException {

        String tgt = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);

        System.out.println("tgt:"+tgt);

        TicketGrantingTicket ticket = centralAuthenticationService.getTicket(tgt,TicketGrantingTicket.class);

        System.out.println("ticket:" + ticket);

        if(ticket != null && !ticket.isExpired()){
            Authentication auth = ticket.getAuthentication();
            System.out.println("auth:" + auth);

            System.out.println("principal:" + auth.getPrincipal());
        }

        return "/default/ui/user/user_test";
    }

}
