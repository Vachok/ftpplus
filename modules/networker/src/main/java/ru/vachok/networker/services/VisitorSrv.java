package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.logic.CookTheCookie;
import ru.vachok.networker.logic.DBMessenger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @since 12.09.2018 (9:44) */
@Service ("visitorSrv")
public class VisitorSrv {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private Visitor visitor;

    private CookieShower cookieShower;

    public Visitor getVisitor() {
        return visitor;
    }

    public CookieShower getCookieShower() {
        return cookieShower;
    }

    /*Instances*/
    @Autowired
    public VisitorSrv(CookieShower cookieShower, Visitor visitor) {
        this.cookieShower = cookieShower;
        this.visitor = visitor;
    }

    public void makeVisit(HttpServletRequest request) throws IllegalArgumentException, NoSuchMethodException {
        visitor.setRemAddr(request.getRemoteAddr());
        visitor.setTimeSt(System.currentTimeMillis());

        MessageToUser viMessageToDB = new DBMessenger();
        viMessageToDB.info(
            new Date(ConstantsFor.START_STAMP) +
                " by: " + visitor.getRemAddr(),
            request.getHeader("USER-AGENT".toLowerCase()),
            request.getCookies().length + " cookies len\n" +
                request.getMethod() + " method\n" +
                TimeUnit.MILLISECONDS
                    .toSeconds(request
                        .getSession().getLastAccessedTime() - request
                        .getSession().getCreationTime()) + " sec spend in application\n" +
                new TForms().fromEnum(request.getSession().getServletContext().getAttributeNames(), true));
        try{
            addCookies(request);
            visitor.setDbInfo(
                new Date(ConstantsFor.START_STAMP) + "\n" +
                    " by: " + visitor.getRemAddr() + "\n" +
                    request.getSession().getId() + "\n" +
                    request.getRequestURL() + " getRequestURL\n" +
                    request.getMethod() + " method\n" +
                    TimeUnit.MILLISECONDS
                        .toSeconds(request
                            .getSession().getLastAccessedTime() - request
                            .getSession().getCreationTime()) + " sec spend.\n" +
                    request.getSession());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("BAD ROBOT! 26.09.2018 (12:37)" +
                this.getClass().getMethod("makeVisit", HttpServletRequest.class).getName());
        }
        LOGGER.info(visitor.toString());
    }

    private void addCookies(HttpServletRequest request) throws IllegalArgumentException {
        Collection<Cookie> cookieCollection = new ArrayList<>(Arrays.asList(request.getCookies()));
        cookieCollection.forEach(x -> LOGGER.info(x.getName()));
    }

    public HttpServletResponse checkSession(HttpServletRequest request, HttpServletResponse response) {
        if(request.getCookies()==null){
            response = new CookTheCookie(visitor).addCookie(response);
        }
        new CookTheCookie(visitor).addToCollection(request);
        return response;
    }
}
