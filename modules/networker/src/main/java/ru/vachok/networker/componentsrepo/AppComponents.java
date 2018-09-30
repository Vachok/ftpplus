package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.logic.CookTheCookie;
import ru.vachok.networker.logic.DBMessenger;
import ru.vachok.networker.services.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@ComponentScan
public class AppComponents {

    @Bean
    public static Logger getLogger() {
        return LoggerFactory.getLogger("ru_vachok_networker");
    }

    @Bean
    public static ADSrv adSrv() {
        ADUser adUser = new ADUser();
        ADComputer adComputer = new ADComputer();
        return new ADSrv(adUser, adComputer);
    }

    @Bean
    @Scope("singleton")
    public Map<String, Boolean> lastNetScanMap() {
        return new LastNetScan().getNetWork();
    }

    @Bean
    @Scope ("singleton")
    public DBMessenger dbMessenger() {
        return new DBMessenger();
    }

    @Bean
    public Map<String, String> getLastLogs() {
        int ind = 10;
        Map<String, String> lastLogsList = new ConcurrentHashMap<>();
        String tbl = "eth";
        Connection c = new RegRuMysql().getDefaultConnection("u0466446_webapp");
        try(PreparedStatement p = c.prepareStatement(String.format("select * from %s ORDER BY timewhen DESC LIMIT 0 , 50", tbl));
            ResultSet r = p.executeQuery()){
            while(r.next()){
                lastLogsList.put(++ind + ") " + r.getString("classname") + " - " + r.getString("msgtype"),
                    r.getString("msgvalue") + " at: " + r.getString("timewhen"));
            }
        }
        catch(SQLException ignore){
            //
        }
        return lastLogsList;
    }

    @Bean("pflists")
    @Scope("singleton")
    public PfLists pfLists() {
        return new PfLists();
    }

    @Bean("visitor")
    @Scope("prototype")
    public Visitor visitor(HttpServletRequest request) {
        return new Visitor(request);
    }

    @Bean
    @Scope("singleton")
    public VisitorSrv visitorSrv(CookieShower cookieShower, Visitor visitor) {
        return new VisitorSrv(cookieShower, visitor);
    }

    @Bean
    public WhoIsWithSRV whoIsWithSRV() {
        return new WhoIsWithSRV();
    }

    @Bean("versioninfo")
    public static VersionInfo versionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        if(ConstantsFor.thisPC().equalsIgnoreCase("home") ||
            ConstantsFor.thisPC().toLowerCase().contains("no0027")){
            versionInfo.setParams();
        }
        return versionInfo;
    }

    public PfListsSrv pfListsSrv(PfLists pfLists) {
        return new PfListsSrv(pfLists);
    }

    @Bean
    @Scope("prototype")
    public CookTheCookie cookTheCookie(Visitor visitor) {
        return new CookTheCookie(visitor);
    }

    @Bean
    @Scope ("singleton")
    public NetScannerSvc netScannerSvc() {
        LastNetScan lastNetScan = new LastNetScan();
        lastNetScan.setNetWork(lastNetScanMap());
        String msg = lastNetScan.getTimeLastScan() + " timeLastScan";
        getLogger().warn(msg);
        return new NetScannerSvc(lastNetScan);
    }
}
