package ru.vachok.money.services;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;

import javax.servlet.http.Cookie;
import java.util.concurrent.TimeUnit;


/**
 @since 23.08.2018 (12:02) */
@Service
public class CookieMaker {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final int MAX_AGE = 600;

    private static final AnnotationConfigApplicationContext ctx = ConstantsFor.CONTEXT;

    private Cookie cookedCookie;

    private final String vachokRu = "vachok.ru";

    Cookie getCookedCookie() {
      defaultCookie();
      return cookedCookie;
   }

   void setCookedCookie(Cookie cookedCookie) {
      this.cookedCookie = cookedCookie;
   }

    private void defaultCookie() {
        cookedCookie = new Cookie(vachokRu, this.getClass().getPackage().getSpecificationTitle());
        cookedCookie.setDomain(vachokRu);
      cookedCookie.setMaxAge(( int ) TimeUnit.MINUTES.toSeconds(15));
      cookedCookie.setSecure(false);
      cookedCookie.setVersion(cookedCookie.hashCode());
   }

    public Cookie startSession(String sessionID) {
        Cookie cookieStart = new Cookie(vachokRu, "money");
        cookieStart.setMaxAge(MAX_AGE);
        cookieStart.setDomain("vachok.ru");
        cookieStart.setComment(sessionID);
        return cookieStart;
    }

    public void chkCookies() {

    }

   @Override
   public int hashCode() {
      return cookedCookie.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      if(this==o) return true;
      if(o==null || getClass()!=o.getClass()) return false;

      CookieMaker that = ( CookieMaker ) o;

      return cookedCookie.equals(that.cookedCookie);
   }
}
