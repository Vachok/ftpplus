package ru.vachok.money.components;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 @since 15.09.2018 (22:08) */
@Component ("cookies")
public class CookIES {

    private String nameCookie;

    private String exprTime;

    private String commentCookie;

    private String domainUser;

    private final Map<Long, String> cookiesToShow = new ConcurrentHashMap<>();

    public Map<Long, String> getCookiesToShow() {
        return cookiesToShow;
    }

    public String getNameCookie() {
        return nameCookie;
    }

    public void setNameCookie(String nameCookie) {
        this.nameCookie = nameCookie;
        cookiesToShow.put(System.currentTimeMillis(), nameCookie);
    }

    public String getExprTime() {
        return exprTime;
    }

    public void setExprTime(String exprTime) {
        this.exprTime = exprTime;
        cookiesToShow.put(System.currentTimeMillis(), exprTime);
    }

    public String getCommentCookie() {
        return commentCookie;
    }

    public void setCommentCookie(String commentCookie) {
        this.commentCookie = commentCookie;
        cookiesToShow.put(System.currentTimeMillis(), commentCookie);
    }

    public String getDomainUser() {
        return domainUser;
    }

    public void setDomainUser(String domainUser) {
        this.domainUser = domainUser;
        cookiesToShow.put(System.currentTimeMillis(), domainUser);
    }

    @Override
    public String toString() {
        return "CookIES{" +
            "commentCookie='" + commentCookie + '\'' +
            ", domainUser='" + domainUser + '\'' +
            ", exprTime='" + exprTime + '\'' +
            ", nameCookie='" + nameCookie + '\'' +
            '}';
    }
}