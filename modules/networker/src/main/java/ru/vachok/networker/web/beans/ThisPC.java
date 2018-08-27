package ru.vachok.networker.web.beans;


import org.slf4j.Logger;
import ru.vachok.networker.ApplicationConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 @since 22.08.2018 (9:59) */
public class ThisPC {

   private static final Logger LOGGER = ApplicationConfiguration.logger();

   private String pcName;

   public String getPcName() {
      return pcName;
   }

   {
      try{
         pcName = InetAddress.getLocalHost().getHostName();
      }
      catch(UnknownHostException e){
         LOGGER.error(e.getMessage(), e);
      }
   }
}
