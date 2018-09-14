package ru.vachok.money.other;


import org.slf4j.Logger;
import ru.vachok.money.config.AppComponents;

import javax.servlet.http.Cookie;
import java.util.concurrent.TimeUnit;


/**
 @since 23.08.2018 (12:02) */
public class CookieMaker {

    private static Logger logger = AppComponents.getLogger();

   private Cookie cookedCookie;

   Cookie getCookedCookie() {
      defaultCookie();
      return cookedCookie;
   }

   void setCookedCookie(Cookie cookedCookie) {
      this.cookedCookie = cookedCookie;
   }

   private void defaultCookie() {
      cookedCookie = new Cookie("vachok.ru", this.getClass().getPackage().getSpecificationTitle());
      cookedCookie.setDomain("vachok.ru");
      cookedCookie.setMaxAge(( int ) TimeUnit.MINUTES.toSeconds(15));
      cookedCookie.setSecure(false);
      cookedCookie.setVersion(cookedCookie.hashCode());
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
