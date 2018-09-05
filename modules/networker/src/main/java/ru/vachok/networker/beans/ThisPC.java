package ru.vachok.networker.beans;


import org.slf4j.Logger;
import ru.vachok.networker.config.AppComponents;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 @since 22.08.2018 (9:59) */
public class ThisPC {

    private static final Logger LOGGER = AppComponents.logger();

   private String pcName;

   public String getPcName() {
      return pcName;
   }

    /*Instances*/
    public ThisPC() {
        try{
            pcName = InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

}
