package ru.vachok.networker.services;


import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vachok.networker.componentsrepo.Visitor;

import javax.servlet.http.Cookie;
import java.util.Collection;

/**
 @since 24.09.2018 (10:44) */
@Component("cookieshower")
public class CookieShower {

    private Collection<Cookie> cookies;

    private Visitor visitor;

    public CookieShower(Visitor visitor) {
        this.visitor = visitor;
        this.cookies = visitor.getCookieCollection();
    }

    public Collection<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(Collection<Cookie> cookies) {
        this.cookies = cookies;
        String cookiesAsString = showCookie();
        LoggerFactory.getLogger(CookieShower.class.getName()).info(cookiesAsString);
        visitor.setCookieCollection(cookies);
    }

    public String showCookie() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Cookie cookie : cookies) {
            stringBuilder.append("<p>");
            String br = "<br>";
            stringBuilder
                .append("Name = ")
                .append(cookie.getName())
                .append(br);
            stringBuilder
                .append("Value = ")
                .append(cookie.getValue())
                .append(br);
            stringBuilder
                .append("Domain = ")
                .append(cookie.getDomain())
                .append(br);
            stringBuilder
                .append("Comment = ")
                .append(cookie.getComment())
                .append(br);
            stringBuilder
                .append("Max age = ")
                .append(cookie.getMaxAge())
                .append(br);
            stringBuilder
                .append("Path = ")
                .append(cookie.getPath())
                .append(br);
            stringBuilder
                .append("Version = ")
                .append(cookie.getVersion())
                .append(br);
            stringBuilder
                .append("Secure = ")
                .append(cookie.getSecure())
                .append(br);
            stringBuilder
                .append("Is HTTP only = ")
                .append(cookie.isHttpOnly())
                .append("</p>");
        }
        return stringBuilder.toString();
    }
}
