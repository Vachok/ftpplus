package ru.vachok.money.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 14.09.2018 (19:48) */
@Service
public class VisitorSrv {

    /*Fields*/
    private CookieMaker cookieMaker;

    public CookieMaker getCookieMaker() {
        return cookieMaker;
    }

    /*Instances*/
    public VisitorSrv(CookieMaker cookieMaker) {
        this.cookieMaker = cookieMaker;
    }

    /**
     {@link }
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VisitorSrv.class.getSimpleName());

    public void makeVisit(HttpServletRequest request, HttpServletResponse response) throws NullPointerException {
        String sessionId = request.getSession().getId();
        String msg = "SET NEW Session ID" + request.getSession().getId() + " sessionID = " + sessionId;
        LOGGER.info(msg);
        if (CookieMaker.isCookiePresent(request) == 0) {
            StringBuilder sb = new StringBuilder();
            Cookie[] cookies = request.getCookies();
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
            new DBMessage().info(VisitorSrv.class.getSimpleName(), "cookies", sb.toString());

        }
        else{
            cookiedResp(response, sessionId);
        }
    }

    private HttpServletResponse cookiedResp(HttpServletResponse response, String sessionId) {
        Cookie cookie = CookieMaker.startSession(sessionId);
        response.addCookie(cookie);
        return response;
    }
}