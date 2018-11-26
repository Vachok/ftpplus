package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.*;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.RuleSet;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.services.ArchivesSorter;
import ru.vachok.networker.services.SimpleCalculator;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;


@ComponentScan
public class AppComponents {

    private static String thisPcName = ConstantsFor.thisPC();

    @Bean
    public static Logger getLogger() {
        Logger logger = LoggerFactory.getLogger("ru_vachok_networker");

        return logger;
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

    @Bean("versioninfo")
    public static VersionInfo versionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        if (thisPcName.equalsIgnoreCase("home") ||
            thisPcName.toLowerCase().contains("no0027")) {
            versionInfo.setParams();
        }
        return versionInfo;
    }

    @Bean
    @Scope("singleton")
    public static ArchivesSorter archivesSorter() {
        return new ArchivesSorter();
    }

    @Bean
    public static List<ADComputer> adComputers() {
        return new ADSrv(new ADUser(), new ADComputer()).getAdComputer().getAdComputers();
    }

    @Bean("simpleCalculator")
    public SimpleCalculator simpleCalculator() {
        return new SimpleCalculator();
    }

    @Bean
    public static PCUserResolver pcUserResolver() {
        return PCUserResolver.getPcUserResolver();
    }

    @Bean
    public ExSRV exSRV() {
        Thread.currentThread().setName("ExSRV");
        return new ExSRV();
    }

    @Bean
    @Scope("Singleton")
    public ConcurrentMap<String, File> getCompUsersMap() {
        ConstantsFor.COMPNAME_USERS_MAP.clear();
        ConstantsFor.COMPNAME_USERS_MAP.put("INIT", new File("."));
        return ConstantsFor.COMPNAME_USERS_MAP;
    }

    /**
     {@link org.springframework.ui.Model} attribute "ruleset"

     @return {@link RuleSet}
     */
    @Bean
    public RuleSet ruleSet() {
        Thread.currentThread().setName("RuleSet");
        return new RuleSet();
    }

    @Bean
    public Map<String, String> getLastLogs() {
        int ind = 10;
        Map<String, String> lastLogsList = new ConcurrentHashMap<>();
        String tbl = "eth";
        Connection c = new RegRuMysql().getDefaultConnection("u0466446_webapp");
        try (PreparedStatement p = c.prepareStatement(String.format("select * from %s ORDER BY timewhen DESC LIMIT 0 , 50", tbl));
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                lastLogsList.put(++ind + ") " + r.getString("classname") + " - " + r.getString("msgtype"),
                    r.getString("msgvalue") + " at: " + r.getString("timewhen"));
            }
        } catch (SQLException ignore) {
            //
        }
        return lastLogsList;
    }

}
