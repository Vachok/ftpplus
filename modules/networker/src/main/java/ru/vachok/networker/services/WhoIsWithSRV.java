

// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.apache.commons.net.whois.WhoisClient;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;


@Service
public class WhoIsWithSRV {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(WhoIsWithSRV.class.getSimpleName());
    
    public WhoIsWithSRV() {
        AppComponents.ipFlushDNS();
    }
    
    public String whoIs(String inetAddr) {
        StringBuilder geoLocation = new StringBuilder();
        Locale locale = Locale.getDefault();
        WhoisClient whoisClient = new WhoisClient();
        String country = locale.getCountry() + " country, " + locale.getLanguage() + " lang";
        geoLocation.append(country).append("</p>");
        geoLocation.append("<p>");
        geoLocation.append("<h3>").append(inetAddr).append("</h3><br>").append("<p>");

        try {
            geoLocation.append(whoIsQuery(inetAddr));
            String replacedStr = geoLocation.toString().replace("% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "% Note: this output has been filtered.\n" +
                "%       To receive output for a database update, use the \"-B\" flag.\n", "");
            if (replacedStr.contains("ERROR:101")) {
                String hostAddress = InetAddress.getByName(inetAddr).getHostAddress();
                replacedStr = new WhoIsWithSRV().whoIs(hostAddress);
                whoisClient.disconnect();
                return replacedStr;
            } else {
                replacedStr = replacedStr + "<p><h4>whois.ripe.net</h4><br>" + whoisClient.query(inetAddr);
                geoLocation.append(replacedStr).append("</p>");
            }
        } catch (IOException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            geoLocation.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        messageToUser.warn(inetAddr + " WHOISED )");
        return geoLocation.toString();
    }
    
    
    private String whoIsQuery(String inetAddr) throws IOException {
        WhoisClient whoisClient = new WhoisClient();
        StringBuilder whoIsQBuilder = new StringBuilder();
        String[] whoisServers = {"whois.ripe.net", "whois.arin.net", "whois.apnic.net", "whois.lacnic.net", "whois.afrinic.net"};
        for (String whoIsServer : whoisServers) {
            whoisClient.connect(whoIsServer);
            String query = whoisClient.query(inetAddr);
            whoIsQBuilder
                .append("<p><h4>")
                .append(whoIsServer)
                .append("</h4><br>")
                .append(query.replaceAll("\n", "<br>"));
            whoisClient.disconnect();
        }
        return whoIsQBuilder.toString();
    }
}