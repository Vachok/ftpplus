package ru.vachok.networker.services;


import org.apache.commons.net.whois.WhoisClient;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;


@Service
public class WhoIsWithSRV {

    /**
     {@link }
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    public static String whoisStat(String workPos, Model model) {
        try {
            WhoIsWithSRV whoIsWithSRV = new WhoIsWithSRV();
            workPos = workPos.split(": ")[1];
            String attributeValue = whoIsWithSRV.whoIs(workPos);
            model.addAttribute(ConstantsFor.WHOIS_STR, attributeValue);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        } catch (ArrayIndexOutOfBoundsException e) {
            model.addAttribute(ConstantsFor.WHOIS_STR, workPos + "<p>" + e.getMessage());
            return ConstantsFor.MATRIX_STRING_NAME;
        }
        return ConstantsFor.MATRIX_STRING_NAME;
    }

    public String whoIs(String inetAddr) {
        StringBuilder geoLocation = new StringBuilder();
        geoLocation.append("<p>");
        Locale locale = Locale.getDefault();
        geoLocation.append("<h3>").append(inetAddr).append("</h3><br>");
        WhoisClient whoisClient = new WhoisClient();
        try {
            geoLocation.append("<p>");
            geoLocation.append(whoIsQuery(inetAddr));
            String replace = geoLocation.toString().replace("% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "% Note: this output has been filtered.\n" +
                "%       To receive output for a database update, use the \"-B\" flag.\n", "");
            if (replace.contains("ERROR:101")) {
                String hostAddress = InetAddress.getByName(inetAddr).getHostAddress();
                replace = new WhoIsWithSRV().whoIs(hostAddress);
                whoisClient.disconnect();
                return replace;
            }
            replace = replace + "<p><h4>whois.ripe.net</h4><br>" + whoisClient.query(inetAddr);
            geoLocation.append(replace).append("</p>");
        } catch (IOException | RuntimeException e) {
            geoLocation.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        String country = locale.getCountry() + " country, " + locale.getLanguage() + " lang";
        geoLocation.append(country).append("</p>");
        String traceRoute = traceRt(inetAddr);
        String msg = geoLocation.toString();
        LOGGER.info(msg);
        return traceRoute + "<p>" + msg;
    }

    private String traceRt(String inetAddr) {
        SSHFactory.Builder sshFactoryBu = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "traceroute " + inetAddr);
        String retStr = sshFactoryBu.build().call();
        try {
            retStr = retStr.split(" = ")[1].replaceAll("(\\s\\d?\\d\\s)", "<br>").trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            return e.getMessage();
        }

        return retStr;
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