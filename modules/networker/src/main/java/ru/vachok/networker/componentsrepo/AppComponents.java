package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.services.ADSrv;
import ru.vachok.networker.services.NetScannerSvc;
import ru.vachok.networker.services.SimpleCalculator;

import java.sql.*;
import java.util.List;
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
    @Scope ("singleton")
    public NetScannerSvc netScannerSvc() {
        LastNetScan lastNetScan = new LastNetScan();
        lastNetScan.setNetWork(lastNetScanMap());
        String msg = lastNetScan.getTimeLastScan() + " timeLastScan";
        getLogger().warn(msg);
        return new NetScannerSvc(lastNetScan, adComputers());
    }

    @Bean
    @Scope("singleton")
    public Map<String, Boolean> lastNetScanMap() {
        return new LastNetScan().getNetWork();
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
        return adSrv().getAdComputer().getAdComputers();
    }

    @Bean
    public SimpleCalculator simpleCalculator() {
        return new SimpleCalculator();
    }

    @Bean
    public ServiceInform serviceInform() {
        ServiceInform serviceInform = new ServiceInform();
        serviceInform.getResourcesTXT();
        return serviceInform;
    }
}
