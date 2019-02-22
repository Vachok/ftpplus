package ru.vachok.networker.componentsrepo;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.SshActs;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.SimpleCalculator;
import ru.vachok.networker.systray.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import static ru.vachok.networker.IntoApplication.getConfigurableApplicationContext;


/**
 Компоненты. Бины

 @since 02.05.2018 (22:14) */
@ComponentScan
public class AppComponents {

    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_VISITOR = "visitor";

    private static MessageToUser messageToUser = new MessageLocal();

    /**
     @return {@link LoggerFactory}
     */
    @Bean
    public static Logger getLogger() {
        return LoggerFactory.getLogger(ConstantsFor.APP_NAME);
    }

    @Bean
    @Scope (ConstantsFor.SINGLETON)
    public static Properties getProps() {
        return getProps(false);
    }

    @Bean
    @Scope (ConstantsFor.SINGLETON)
    public static Properties getProps(boolean saveThis) {
        Properties properties = ConstantsFor.getAppProps();
        if(saveThis){
            boolean isSaved = ConstantsFor.saveAppProps(properties);
            new MessageToTray().info("AppComponents.getProps ", " isSaved", " = " + isSaved);
        }
        return properties;
    }

    @Bean
    public Connection connection(String dbName) throws SQLException, IOException {
        OutputStream outputStream = new FileOutputStream("AppComponents.connection.log");
        PrintWriter printWriter = new PrintWriter(outputStream, true);
        try{
            MysqlDataSource dataSource = new RegRuMysql().getDataSourceSchema(dbName);
            dataSource.setAutoReconnect(true);
            dataSource.setLogWriter(printWriter);
            return dataSource.getConnection();
        }
        catch(Exception e){
            messageToUser.errorAlert("AppComponents", ConstantsNet.STR_CONNECTION, e.getMessage());
            FileSystemWorker.error("AppComponents.connection", e);
            outputStream.close();
            return new RegRuMysql().getDefaultConnection(dbName);
        }
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
    @Scope (ConstantsFor.SINGLETON)
    public SshActs sshActs() {
        return new SshActs();
    }

    @Bean (STR_VISITOR)
    public Visitor visitor(HttpServletRequest request) {
        return new Visitor(request);
    }

    @Bean
    @Scope (ConstantsFor.SINGLETON)
    public static NetPinger netPinger() {
        return new NetPinger();
    }

    @Bean
    @Scope (ConstantsFor.SINGLETON)
    public static ThreadConfig threadConfig() {
        return ThreadConfig.getI();
    }

    @Bean
    @Scope (ConstantsFor.SINGLETON)
    public static NetScannerSvc netScannerSvc() {
        return NetScannerSvc.getInst();
    }

    /**
     @return {@link #lastNetScan()}.getNetWork
     */
    @Bean
    @Scope (ConstantsFor.SINGLETON)
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
    @Bean ("versioninfo")
    @Scope (ConstantsFor.SINGLETON)
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

    public static Visitor thisVisit(String sessionID) throws NullPointerException, NoSuchBeanDefinitionException {
        return ( Visitor ) configurableApplicationContext().getBean(sessionID);
    }

    @Bean
    @Scope (ConstantsFor.SINGLETON)
    static ConfigurableApplicationContext configurableApplicationContext() {
        return getConfigurableApplicationContext();
    }

    @Override
    public String toString() {
        ConfigurableApplicationContext context = getConfigurableApplicationContext();
        final StringBuilder sb = new StringBuilder("AppComponents{");
        sb.append("Beans=").append(new TForms().fromArray(context.getBeanDefinitionNames(), false)).append("\n");
        sb.append(context);
        sb.append('}');
        return sb.toString();
    }
}
