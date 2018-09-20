package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.Visitor;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.other.MailMessages;
import ru.vachok.money.services.CookieMaker;
import ru.vachok.money.services.TForms;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 20.08.2018 (17:08) */
@Controller
public class Index {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final AnnotationConfigApplicationContext ctx = ConstantsFor.CONTEXT;

    private static DataConnectTo dataConnectTo = new RegRuMysql();

    private static CookieMaker cookieMaker = new CookieMaker();

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final AnnotationConfigApplicationContext ctx = ConstantsFor.CONTEXT;

    private static CookieMaker cookieMaker = new CookieMaker();

    private MailMessages mailMessages = new MailMessages();

    @GetMapping ("/")
    public String indexString(HttpServletRequest request, HttpServletResponse response, Model model) {
        VisitorSrv visitorSrv = ctx.getBean(VisitorSrv.class);
        Visitor visitor = ctx.getBean(Visitor.class);
        DeviceLocator deviceLocator = ctx.getBean(DeviceLocator.class);
        visitor.setSessionId(request.getSession().getId());
        visitorSrv.makeVisit(request, response);
        model.addAttribute("title", request.getRemoteAddr() + " " + response.getStatus());
        model.addAttribute("locator", deviceLocator.searchGeoLoc(request));
        return "index-start";
    }

    private boolean chkCookies(HttpServletRequest request) {
        Cookie[] cookies;
        try{
            cookies = request.getCookies();
            if(cookies.length > 0){
                CookIES bean = ctx.getBean(CookIES.class);
                for(Cookie c : cookies){
                    bean.setCommentCookie(c.getComment());
                    bean.setDomainUser(c.getDomain());
                    bean.setExprTime(c.getMaxAge() + " sec");
                    bean.setNameCookie(c.getName());
                    LOGGER.info(bean.toString());
                }
                return false;
            }
        }
        catch(NullPointerException e){
            return true;
        }
        return true;
    }
    private Cookie addCookie(HttpServletRequest request) {
        String sessionID = request.getSession().getId();
        LOGGER.info("Cookie set");
        return cookieMaker.startSession(sessionID);
    }

    public Model mailMsg(Model model, HttpServletRequest request) {
        Folder inbox = mailMessages.getInbox();
        MessageFromInbox bean = ctx.getBean(MessageFromInbox.class);
        try{
            model.addAttribute("mailbox", " you have  <<<" + inbox.getMessageCount() + ">>>  messages" +
                "\n" + mailMessages.getMailBox(false));
            model.addAttribute("message", bean.toString());
        }
        catch(MessagingException e){
            LOGGER.error(e.getMessage(), e);
        }
        return model;
    }
}
