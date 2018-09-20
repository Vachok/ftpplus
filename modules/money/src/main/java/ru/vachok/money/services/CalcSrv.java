package ru.vachok.money.services;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @since 19.09.2018 (21:44) */
@Service ("CalcSrv")
public class CalcSrv {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CalcSrv.class.getSimpleName();

    /**
     {@link }
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final AnnotationConfigApplicationContext CONTEXT = ConstantsFor.CONTEXT;

    public String resultCalc(String userInp) {
        LOGGER.info(userInp);
        if(userInp.equalsIgnoreCase("time")){
            return time(userInp);
        }
        if(userInp.toLowerCase().contains("whois:")|| userInp.toLowerCase().contains("wh:")){
            userInp=userInp.split(":")[1].trim();
            return whoIs(userInp);
        }
        return userInp;
    }

    private String time(String userInp) {
        LOGGER.info(userInp);
        return System.currentTimeMillis() + " uTime<br>" + ( float ) TimeUnit.MILLISECONDS
            .toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP) / 60 + " min Uptime<br>" +
            new Date(ConstantsFor.START_STAMP);

    }
    private String whoIs(String userInp){
        WhoIsWithSRV bean = new WhoIsWithSRV();
        String s = bean.whoIs(userInp);
        return s;
    }
}