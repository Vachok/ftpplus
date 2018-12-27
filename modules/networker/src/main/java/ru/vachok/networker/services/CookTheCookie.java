package ru.vachok.networker.services;


import org.springframework.beans.factory.annotation.Autowired;
import ru.vachok.networker.componentsrepo.Visitor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 @since 24.09.2018 (12:33) */
public class CookTheCookie {

    private Visitor visitor;

    @Autowired
    public CookTheCookie(Visitor visitor) {
        this.visitor = visitor;
    }

    public HttpServletResponse addCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("Clicks", visitor.getClickCounter() + "");
        cookie.setDomain("vachok.ru");
        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(1));
        response.addCookie(cookie);
        return response;
    }
}
