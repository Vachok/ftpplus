package ru.vachok.money.services;


import org.apache.commons.net.whois.WhoisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;


/**
 @since 14.09.2018 (22:46) */
@Service ("locator")
public class WhoIsWithSRV {

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = WhoIsWithSRV.class.getSimpleName();

    /*Fields*/
    /**
     {@link }
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SOURCE_CLASS);

    public String whoIs(String inetAddr) {
        StringBuilder geoLocation = new StringBuilder();
        Locale locale = Locale.getDefault();
        geoLocation
            .append("<b><h2>")
            .append(inetAddr)
            .append("</b></h2>");
        WhoisClient whoisClient = new WhoisClient();
        try{geoLocation.append("<p>");
            whoisClient.connect("whois.ripe.net");
            String queryWhoIs = whoisClient.query(inetAddr);
            queryWhoIs= queryWhoIs.replaceAll("\\Q%\\E", "<br>");
            queryWhoIs = queryWhoIs.replace("<br> The objects are in RPSL format.\n" +
                "<br>\n" +
                "<br> The RIPE Database is subject to Terms and Conditions.\n" +
                "<br> See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "<br> Note: this output has been filtered.\n" +
                "<br>       To receive output for a database update, use the \"-B\" flag.", "");
            if(queryWhoIs.toLowerCase().contains("ERROR")){
                String hostAddress = InetAddress.getByName(inetAddr).getHostAddress();
                String msg = hostAddress + " resolved by " + SOURCE_CLASS;
                LOGGER.warn(msg);
                geoLocation.append( new WhoIsWithSRV().whoIs(hostAddress));
            }
            else{
                geoLocation.append(queryWhoIs.replaceAll("\n", "<br>")).append("</p>");
            }
            whoisClient.disconnect();
        }
        catch(IOException | RuntimeException e){
            geoLocation.append(e.getMessage()).append("\n").append(new TForms().toStringFromArray(e));
        }
        String country = locale.getDisplayCountry() + " - " + locale.getDisplayLanguage();
        geoLocation.append(country).append("</p>");
        return geoLocation.toString();
    }

    public String resolveLocation(HttpServletRequest request){
        StringBuilder locResolved = new StringBuilder();
        locResolved.append(new TForms().enumToString(request.getHeaderNames(), true));
        return locResolved.toString();
    }
}