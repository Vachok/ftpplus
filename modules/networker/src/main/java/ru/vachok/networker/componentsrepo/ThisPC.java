package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 @since 22.08.2018 (9:59) */
public class ThisPC {

    private static final Logger LOGGER = AppComponents.getLogger();

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
