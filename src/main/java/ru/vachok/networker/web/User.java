package ru.vachok.networker.web;


/**
 @since 20.08.2018 (16:03) */
public class User {

   private final String john;

   private final String apricot;

   private final String antarctica;

   private final Object o;

   public User(String john, String apricot, String antarctica, Object o) {
      super();
      this.john = john;
      this.apricot = apricot;
      this.antarctica = antarctica;
      this.o = o;
   }

   String getJohn() {
      return john;
   }

   String getApricot() {
      return apricot;
   }

   String getAntarctica() {
      return antarctica;
   }

   Object getO() {
      return o;
   }
}
