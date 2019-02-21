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
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.services.SimpleCalculator;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Properties;
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
        return LoggerFactory.getLogger(ConstantsFor.APP_NAME);
    }

    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static Properties getProps() {
        return getProps(false);
    }

    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static NetPinger netPinger() {
        return new NetPinger();
    }

    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static ThreadConfig threadConfig() {
        return ThreadConfig.getI();
    }

    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static NetScannerSvc netScannerSvc() {
        return NetScannerSvc.getInst();
    }
    /**
     @return {@link #lastNetScan()}.getNetWork
     */
    @Bean
    @Scope(ConstantsFor.SINGLETON)
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
    @Scope(ConstantsFor.SINGLETON)
    public static VersionInfo versionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        boolean isBUGged = false;
        if(new File("bugged").exists()){
            isBUGged = true;
        }
        versionInfo.setBUGged(isBUGged);
        return versionInfo;
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

    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static Properties getProps(boolean saveThis) {
        Properties properties = ConstantsFor.getAppProps();
        if (saveThis) {
            ConstantsFor.saveAppProps(properties);
        }
        return properties;
    }

    public static Visitor thisVisit(String sessionID) throws NullPointerException, NoSuchBeanDefinitionException {
        return (Visitor) configurableApplicationContext().getBean(sessionID);
    }

    @Bean
    @Scope(ConstantsFor.SINGLETON)
    static ConfigurableApplicationContext configurableApplicationContext() {
        return IntoApplication.getConfigurableApplicationContext();
    }

    @Bean
    public Connection connection(String dbName) throws SQLException {
        Connection connection = new RegRuMysql().getDefaultConnection(dbName);
        Savepoint startSavepoint = connection.setSavepoint("start");
        return connection;
    }

    /**
     @return new {@link SimpleCalculator}
     */
    @Bean(ConstantsFor.STR_CALCULATOR)
    public SimpleCalculator simpleCalculator() {
        return new SimpleCalculator();
    }

    /**
     @return new {@link SshActs}
     */
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public SshActs sshActs() {
        return new SshActs();
    }

    @Bean(STR_VISITOR)
    public Visitor visitor(HttpServletRequest request) {
        return new Visitor(request);
    }

}
