package ru.vachok.money.services;


import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.util.CookieGenerator;
import ru.vachok.money.config.AppComponents;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 23.08.2018 (12:02) */
@Service ("cookiesmaker")
public class CookieMaker extends CookieGenerator {

    /*Fields*/

    /**
     Max cookies life
     */
    private static final int MAX_AGE = 6000;

    /**
     Имя атрибута модели
     */
    private static final String AT_NAME_VACHOKRU = "vachokru";

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger THIS_LOGGER = AppComponents.getLogger();

    /**
     Добавим куки
     <p>
     Usages: {@link VisitorSrv#cookiedResp(HttpServletResponse, String)} <br>
     Uses: - <br>

     @param sessionID ID http-сессии
     @return new {@link Cookie}
     */
    public static Cookie startSession(String sessionID) {
        Cookie cookieStart = new Cookie(AT_NAME_VACHOKRU, sessionID);
        cookieStart.setMaxAge(MAX_AGE);
        cookieStart.setDomain(AT_NAME_VACHOKRU);
        cookieStart.setComment(System.currentTimeMillis() + "");
        return cookieStart;
    }

    /**
     Проверка наличия {@link Cookie}
     <p>
     Usages: {@link VisitorSrv#makeVisit(HttpServletRequest, HttpServletResponse)} <br>
     Uses: - <br>

     @param request {@link HttpServletRequest}
     @return кол-во {@link Cookie}
     */
    static int isCookiePresent(HttpServletRequest request) {
        int length = 0;
        try{
            length = request.getCookies().length;
        }
        catch(NullPointerException e){
            THIS_LOGGER.warn(e.getMessage(), " no cookies");
        }
        if(length > 0){
            return length;
        }
        else{
            return 0;
        }
    }
}
