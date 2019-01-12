package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.accesscontrol.SshActs;
import ru.vachok.networker.accesscontrol.common.CommonScan2YOlder;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.ADUser;
import ru.vachok.networker.mailserver.RuleSet;
import ru.vachok.networker.services.SimpleCalculator;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 Компоненты. Бины

 @since 02.05.2018 (22:14) */
@ComponentScan
public class AppComponents {

    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_VISITOR = "visitor";

    /**
     @return {@link LoggerFactory}
     */
    @Bean
    public static Logger getLogger() {
        return LoggerFactory.getLogger("ru_vachok_networker");
    }

    /**
     @return new {@link SimpleCalculator}
     */
    @Bean (ConstantsFor.STR_CALCULATOR)
    public SimpleCalculator simpleCalculator() {
        return new SimpleCalculator();
    }

    /**
     @return new {@link SshActs}
     */
    @Bean
    @Scope ("singleton")
    public SshActs sshActs() {
        return new SshActs();
    }

    @Bean (STR_VISITOR)
    public Visitor visitor(HttpServletRequest request) {
        return new Visitor(request);
    }

    /**
     @return {@link #lastNetScan()}.getNetWork
     */
    @Bean
    @Scope("singleton")
    public static ConcurrentMap<String, Boolean> lastNetScanMap() {
        return lastNetScan().getNetWork();
    }

    /**
     @return {@link LastNetScan#getLastNetScan()}
     */
    @Bean
    public static LastNetScan lastNetScan() {
        return LastNetScan.getLastNetScan();
    }

    /**
     @return new {@link VersionInfo}
     */
    @Bean("versioninfo")
    @Scope("singleton")
    public static VersionInfo versionInfo() {
        return new VersionInfo();
    }

    /**
     new {@link ADComputer} + new {@link ADUser}

     @return new {@link ADSrv}
     */
    @Bean
    public static ADSrv adSrv() {
        ADUser adUser = new ADUser();
        ADComputer adComputer = new ADComputer();
        return new ADSrv(adUser, adComputer);
    }

    public static Visitor thisVisit(String sessionID) throws InvocationTargetException, NullPointerException, NoSuchBeanDefinitionException {
        return ( Visitor ) configurableApplicationContext().getBean(sessionID);
    }

    /**
     @return new {@link CommonScan2YOlder}
     */
    @Bean
    @Scope("singleton")
    public static CommonScan2YOlder archivesSorter() {
        return new CommonScan2YOlder();
    }

    /**
     @return new {@link ADSrv}(new {@link ADUser}, new {@link ADComputer#getAdComputers()}.getAdComputers)
     */
    @Bean
    public static List<ADComputer> adComputers() {
        return new ADSrv(new ADUser(), new ADComputer()).getAdComputer().getAdComputers();
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

    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static ConfigurableApplicationContext configurableApplicationContext() {
        return IntoApplication.getConfigurableApplicationContext();
    }

}
