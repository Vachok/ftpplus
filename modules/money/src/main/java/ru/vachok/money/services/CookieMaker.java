package ru.vachok.money.services;


import org.springframework.stereotype.Service;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;


/**
 * @since 23.08.2018 (12:02)
 */
@Service("cookiesmaker")
public class CookieMaker extends CookieGenerator {

    private static final int MAX_AGE = 6000;

    private static final String vachokRu = "vachokru";

    private Cookie defaultCookie() {
        Cookie cookedCookie = new Cookie(vachokRu, "Hi!");
        cookedCookie.setDomain(vachokRu);
        cookedCookie.setMaxAge((int) TimeUnit.HOURS.toSeconds(15));
        cookedCookie.setSecure(false);
        return cookedCookie;
    }

    public static Cookie startSession(String sessionID) {
        Cookie cookieStart = new Cookie(vachokRu, sessionID);
        cookieStart.setMaxAge(MAX_AGE);
        cookieStart.setDomain(vachokRu);
        cookieStart.setComment(System.currentTimeMillis()+"");
        return cookieStart;
    }

    static int isCookiePresent(HttpServletRequest request) {
        int length = request.getCookies().length;
        if( length >0) return length;
        else return 0;
    }
}
