package ru.vachok.money.services;


import org.apache.commons.net.whois.WhoisClient;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;


/**
 @since 14.09.2018 (22:46) */
@Service ("locator")
public class DeviceLocator {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = DeviceLocator.class.getSimpleName();

    /**
     {@link }
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final AnnotationConfigApplicationContext ctx = ConstantsFor.CONTEXT;

    public String searchGeoLoc(HttpServletRequest request) {
        StringBuilder geoLocation = new StringBuilder();
        geoLocation.append("<p>");
        Locale locale = Locale.getDefault();
        String inetAddress = request.getRemoteAddr();
        geoLocation.append(inetAddress).append("<br>");
        WhoisClient whoisClient = new WhoisClient();
        try{
            String queryWhoIs = whoisClient.query(inetAddress);
            LOGGER.info(queryWhoIs);
            geoLocation.append(queryWhoIs).append("<br>");
        }
        catch(IOException | RuntimeException e){
            geoLocation.append(e.getMessage()).append("\n").append(new TForms().toStringFromArray(e));
        }
        String country = locale.getCountry() + " " + locale.getLanguage();
        LOGGER.info(country);

        geoLocation.append(country).append("</p>");
        return geoLocation.toString();
    }
}