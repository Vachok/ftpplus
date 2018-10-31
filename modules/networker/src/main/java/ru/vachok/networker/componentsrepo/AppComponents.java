package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.ADUser;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.RulesBean;
import ru.vachok.networker.services.NetScannerSvc;
import ru.vachok.networker.services.PCUserResolver;
import ru.vachok.networker.services.SimpleCalculator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;


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
    public static ReentrantLock lock() {
        return new ReentrantLock();
    }

    @Bean
    public static NetScannerSvc netScannerSvc() {
        return NetScannerSvc.getI();
    }

    @Bean
    @Scope("singleton")
    public static ConcurrentMap<String, Boolean> lastNetScanMap() {
        return lastNetScan().getNetWork();
    }

    @Bean
    public static LastNetScan lastNetScan() {
        return LastNetScan.getLastNetScan();
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

    @Bean("versioninfo")
    public static VersionInfo versionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        if(ConstantsFor.thisPC().equalsIgnoreCase("home") ||
            ConstantsFor.thisPC().toLowerCase().contains("no0027")){
            versionInfo.setParams();
        }
        return versionInfo;
    }

    @Bean
    public static List<ADComputer> adComputers() {
        return new ADSrv(new ADUser(), new ADComputer()).getAdComputer().getAdComputers();
    }

    @Bean
    public SimpleCalculator simpleCalculator() {
        return new SimpleCalculator();
    }

    @Bean
    public static PCUserResolver pcUserResolver() {
        return PCUserResolver.getPcUserResolver();
    }

    @Bean
    public ExSRV exSRV() {
        RulesBean rulesBean = new RulesBean();
        return new ExSRV(rulesBean);
    }
}
