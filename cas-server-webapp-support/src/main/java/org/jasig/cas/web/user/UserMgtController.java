package org.jasig.cas.web.user;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.user.UserDao;
import org.jasig.cas.util.UserUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author luopeng
 *         Created on 2015/4/9.
 */
@Controller
@RequestMapping("/user")
public class UserMgtController {

    @Resource
    private UserDao userDao;

    @Resource
    private UserUtils userUtils;

    private String updatePasswdUrl = "/mgt/user/update_passwd";

    @RequestMapping(value = "/update_passwd", method = RequestMethod.GET)
    public String updatePass(HttpServletRequest request, String code) {
        request.setAttribute("code", code);
        return "default/ui/user/updatePasswd";
    }

    @RequestMapping(value = "/update_passwd", method = RequestMethod.POST)
    public String postUpdatePasswd(HttpServletRequest request, HttpServletResponse response, String oldPasswd, String newPasswd,
                                   String confirmNewPasswd,String captcha) throws InvalidTicketException {

        if(!validateCaptcha(request,captcha)){
            return sendRedirectToUpdatePasswd(request,"captcha_error");
        }

        if (StringUtils.isBlank(oldPasswd)){
            return sendRedirectToUpdatePasswd(request, "passwd_miss");
        }

        if (StringUtils.isBlank(newPasswd) || !StringUtils.equals(newPasswd, confirmNewPasswd)) {
            return sendRedirectToUpdatePasswd(request, "confirm_passwd_mismatch");
        }

        String loginName = userUtils.getLoginPrincipal(request);

        if (!userDao.updatePasswd(newPasswd,loginName, oldPasswd)) {
            return sendRedirectToUpdatePasswd(request, "failure");
        }

        return sendRedirectToUpdatePasswd(request, "ok");
    }

    private String sendRedirectToUpdatePasswd(HttpServletRequest request, String code) {
        return "redirect:" + request.getContextPath() + updatePasswdUrl + "/?code=" + code;
    }

    /**
     * validate captcha
     * @param request
     * @return true if captcha valid
     */
    private boolean validateCaptcha(HttpServletRequest request,String captcha) {
        HttpSession session = request.getSession();
        String sessionCaptcha = (String)session.getAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);

        //remove
        session.removeAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);

        return !org.springframework.util.StringUtils.isEmpty(captcha) && org.springframework.util.StringUtils.endsWithIgnoreCase(sessionCaptcha, captcha);
    }

}
