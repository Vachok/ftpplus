package ru.vachok.networker.web.controller;



import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.web.ApplicationConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @since 21.08.2018 (14:40)
 */
@Controller
public class NetScanner {


    @GetMapping("/netscan")
    public String getPCNames( HttpServletRequest request , Model model ) throws IOException {
        List<String> pcNames = new ArrayList<>();
        String nameCount;
        InetAddress inetAddress;
        String qer = request.getQueryString();
        String namePref = "do";
        if (qer != null) {
            ApplicationConfiguration.logger().info(qer);
            namePref = qer;
        }
        for (int i = 1; i < 350; i++) {
            nameCount = String.format("%04d" , i);
            if (namePref.equalsIgnoreCase("a")) nameCount = String.format("%03d" , i);
            if (namePref.equalsIgnoreCase("td")) nameCount = String.format("%03d" , i);
            try {
                inetAddress = InetAddress.getByName(namePref + nameCount + ".eatmeat.ru");
            } catch (UnknownHostException e) {
                continue;
            }
            boolean reachable = inetAddress.isReachable(100);
            String e = inetAddress.toString();
            if (!reachable) {
                String onLines = ("<b> online </b><i>" + false + "</i>");
                pcNames.add(e + "<b>" + onLines + "</b>");
                ApplicationConfiguration.logger().warn(e + " " + onLines);
            } else {
                String onLines = ("<b> online </b>" + true);
                pcNames.add(e + "<b>" + onLines + "</b>");
                ApplicationConfiguration.logger().warn(e + " " + onLines);
            }
        }
        boolean b = writeDB(pcNames);
        pcNames.add(b + " WRITE TO DB");
        model.addAttribute("pc" , "<p>" + Arrays.toString(pcNames.toArray()).replaceAll(", " , "<br>") + "</p>");
        return "netscan";
    }


    private boolean writeDB( List<String> pcNames ) {
        DataConnectTo dataConnectTo = new RegRuMysql();

        try (Connection c = dataConnectTo.getDataSource().getConnection(); PreparedStatement p = c.prepareStatement("insert into  u0466446_liferpg.velkompc (NamePP, AddressPP, OnlineNow) values(?,?,?)")) {
            pcNames.stream().sorted().forEach(x -> {
                boolean onLine = false;
                try {
                    if (x.contains("true")) onLine = true;
                    p.setString(1 , x.split("/")[0]);
                    p.setString(2 , x.split("/")[1].split(" ")[0].replaceAll("<b>" , ""));
                    p.setBoolean(3 , onLine);
                    p.executeUpdate();
                } catch (SQLException e) {
                    ApplicationConfiguration.logger().error(e.getMessage() , e);
                }
            });
            return true;
        } catch (SQLException e) {
            ApplicationConfiguration.logger().error(e.getMessage() , e);
            return false;
        }
    }
}
