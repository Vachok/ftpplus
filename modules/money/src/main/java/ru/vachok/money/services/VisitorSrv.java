package ru.vachok.money.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;


/**
 @since 14.09.2018 (19:48) */
@Service
public class VisitorSrv {

    /*Fields*/

    /**
     {@link }
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorSrv.class.getSimpleName());

    private CookieMaker cookieMaker;

    public CookieMaker getCookieMaker() {
        return cookieMaker;
    }
    /*Instances*/
    public VisitorSrv(CookieMaker cookieMaker) {
        this.cookieMaker = cookieMaker;
    }

    public void makeVisit(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException {
        StringBuilder sb = new StringBuilder();
        StringBuilder req = new StringBuilder();
        String sessionId = request.getSession().getId();
        String msg = "SET NEW Session ID" + request.getSession().getId() + " sessionID = " + sessionId;
        LOGGER.info(msg);
        if(CookieMaker.isCookiePresent(request)==0){
            Cookie[] cookies = request.getCookies();
            try{
                for(Cookie c : cookies){
                    sb
                        .append("Name: ")
                        .append(c.getName()).append("\n")
                        .append("Age: ")
                        .append(c.getMaxAge()).append("\n")
                        .append("Comment: ")
                        .append(c.getComment()).append("\n")
                        .append("Value")
                        .append(c.getValue());
                }
            }
            catch(NullPointerException e){
                String msg1 = e.getMessage() + " no cookies";
                LOGGER.warn(msg1);
            }
        }
        else{
            cookiedResp(response, sessionId);
        }
        String n = "\n";
        Cookie[] cookies = new Cookie[100];
        try{
            cookies = request.getCookies();
        }
        catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        req
            .append(request.getRemoteAddr()).append(" address")
            .append(n)
            .append(request.getMethod()).append(" method")
            .append(n)
            .append(request.getLocale()).append(" Locale")
            .append(n)
            .append(request.getSession().getId()).append(" ID")
            .append(n)
            .append(TimeUnit.MILLISECONDS.toMinutes(request.getSession().getCreationTime() - System.currentTimeMillis())).append(" min")
            .append(n)
            .append(request.getRemoteHost()).append(" host")
            .append(n)
            .append(new TForms().toStringFromArray(cookies, false));

        new DBMessage().info(VisitorSrv.class.getSimpleName(), "httpreq", req.toString());
    }

    private HttpServletResponse cookiedResp(HttpServletResponse response, String sessionId) {
        Cookie cookie = CookieMaker.startSession(sessionId);
        response.addCookie(cookie);
        return response;
    }
}