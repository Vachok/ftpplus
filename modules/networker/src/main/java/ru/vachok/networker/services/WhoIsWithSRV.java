package ru.vachok.networker.services;


import org.apache.commons.net.whois.WhoisClient;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;


/**
 * @since 14.09.2018 (22:46)
 */
@Service("locator")
public class WhoIsWithSRV {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = WhoIsWithSRV.class.getSimpleName();

    /**
     * {@link }
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final AnnotationConfigApplicationContext ctx = IntoApplication.getAppCtx();

    public String whoIs(String inetAddr) {
        StringBuilder geoLocation = new StringBuilder();
        geoLocation.append("<p>");
        Locale locale = Locale.getDefault();
        geoLocation.append(inetAddr).append("<br>");
        WhoisClient whoisClient = new WhoisClient();
        try {
            geoLocation.append("<p>");
            whoisClient.connect("whois.ripe.net");
            String queryWhoIs = whoisClient.query(inetAddr);
            queryWhoIs = queryWhoIs.replace("% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "% Note: this output has been filtered.\n" +
                "%       To receive output for a database update, use the \"-B\" flag.\n", "");
            if (queryWhoIs.contains("ERROR:101")) {
                String hostAddress = InetAddress.getByName(inetAddr).getHostAddress();
                queryWhoIs = new WhoIsWithSRV().whoIs(hostAddress);
                return queryWhoIs;
            }

            geoLocation.append(queryWhoIs).append("</p>");
            whoisClient.disconnect();
        } catch (IOException | RuntimeException e) {
            geoLocation.append(e.getMessage()).append("\n").append(new TForms().fromArray(e.getStackTrace()));
        }
        String country = locale.getCountry() + " country, " + locale.getLanguage() + " lang";
        geoLocation.append(country).append("</p>");

        String msg = geoLocation.toString();
        LOGGER.info(msg);
        return msg;
    }
}