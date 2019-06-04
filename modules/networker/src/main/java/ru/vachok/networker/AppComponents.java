// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.jcraft.jsch.JSch;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.accesscontrol.TemporaryFullInternet;
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.ftp.RegRuFTPLibsUploader;
import ru.vachok.networker.services.ADSrv;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.SimpleCalculator;
import ru.vachok.stats.SaveLogsToDB;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 Компоненты. Бины

 @since 02.05.2018 (22:14) */
@ComponentScan
public class AppComponents {


    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_VISITOR = "visitor";

    private static final Properties APP_PR = new Properties();

    private static final ConcurrentMap<Long, Visitor> VISITS_MAP = new ConcurrentHashMap<>();
    
    private static final String DB_JAVA_ID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
    
    private static MessageToUser messageToUser = new MessageLocal(AppComponents.class.getSimpleName());

    public static ConcurrentMap<Long, Visitor> getVisitsMap() {
        return VISITS_MAP;
    }

    @Bean
    public TemporaryFullInternet temporaryFullInternet() {
        TemporaryFullInternet temporaryFullInternet = new TemporaryFullInternet();
        messageToUser.info("AppComponents.temporaryFullInternet", "temporaryFullInternet.hashCode()", " = " + temporaryFullInternet.hashCode());
        return temporaryFullInternet;
    }

    @Bean
    public static Logger getLogger(String className) {
        return LoggerFactory.getLogger(className);
    }
    
    public static String ipFlushDNS() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Process processFlushDNS = Runtime.getRuntime().exec("ipconfig /flushdns");
            InputStream flushDNSInputStream = processFlushDNS.getInputStream();
            InputStreamReader reader = new InputStreamReader(flushDNSInputStream);
            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                bufferedReader.lines().forEach(stringBuilder::append);
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return stringBuilder.toString();
    }

    @Bean
    public Connection connection(String dbName) throws IOException {

        try {
            MysqlDataSource dataSource = new RegRuMysql().getDataSourceSchema(dbName);
            File dsVarFile = new File("datasrc." + dataSource.hashCode());
            dataSource.setAutoReconnect(true);
            dataSource.setRelaxAutoCommit(true);
            dataSource.setInteractiveClient(true);
            return dataSource.getConnection();
        } catch (SQLException e) {
            messageToUser.errorAlert("AppComponents", ConstantsNet.STR_CONNECTION, e.getMessage());
            FileSystemWorker.error("AppComponents.connection", e);
            return new RegRuMysql().getDefaultConnection(dbName);
        }
    }

    /**
     @return new {@link SimpleCalculator}
     */
    @Bean(ConstantsFor.BEANNAME_CALCULATOR)
    public SimpleCalculator simpleCalculator() {
        return new SimpleCalculator();
    }

    /**
     SSH-actions.
     <p>
     Через библиотеку {@link JSch}

     @return new {@link SshActs}
     */
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public SshActs sshActs() {
        SshActs sshActs = new SshActs();
        messageToUser.info("AppComponents.sshActs", " sshActs.hashCode()", " = " + sshActs.hashCode());
        return sshActs;
    }

    @Bean(STR_VISITOR)
    public Visitor visitor(HttpServletRequest request) {
        Visitor visitor = new Visitor(request);
        return VISITS_MAP.putIfAbsent(request.getSession().getCreationTime(), visitor);
    }
    
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public SaveLogsToDB saveLogsToDB() {
        return new SaveLogsToDB();
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
     @return new {@link VersionInfo}
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Bean(ConstantsFor.STR_VERSIONINFO)
    @Scope(ConstantsFor.SINGLETON)
    public static VersionInfo versionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        if (ConstantsFor.thisPC().toLowerCase().contains("home") && ConstantsFor.thisPC().toLowerCase().contains("do00213")) {
            versionInfo.setParams();
        }
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

    public static AbstractBeanFactoryBasedTargetSource configurableApplicationContext() {
        throw new IllegalComponentStateException("Moved to: " + IntoApplication.class.getSimpleName());
    }
    
    public boolean updateProps(Properties propertiesToUpdate) throws IOException {
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
        boolean isDel = initProperties.delProps();
        boolean isSet = initProperties.setProps(propertiesToUpdate);
        propertiesToUpdate.store(new FileOutputStream(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES), getClass().getSimpleName() + ".updateProps");
        return isDel & isSet;
    }
    
    
    public static Properties getProps() {
        if (APP_PR.size() > 3) {
            if ((APP_PR.getProperty(ConstantsFor.PR_DBSTAMP) != null) && (Long.parseLong(APP_PR.getProperty(ConstantsFor.PR_DBSTAMP)) + TimeUnit.MINUTES.toMillis(180)) < System
                .currentTimeMillis()) {
                APP_PR.putAll(new AppComponents().getAppProps());
            }
            return APP_PR;
        }
        else {
            Properties appProps = new AppComponents().getAppProps();
            appProps.setProperty(ConstantsFor.PR_DBSTAMP, String.valueOf(System.currentTimeMillis()));
            appProps.setProperty(ConstantsFor.PR_THISPC, ConstantsFor.thisPC());
            return appProps;
        }
    }
    
    public void updateProps() throws IOException {
        if (APP_PR.size() > 3) {
            updateProps(APP_PR);
        }
        else {
            throw new IllegalComponentStateException("Properties to small : " + APP_PR.size());
        }
    }
    
    private static boolean saveAppPropsForce() {
        DBPropsCallable saveDBPropsCallable = new DBPropsCallable(new DBRegProperties(DB_JAVA_ID).getRegSourceForProperties(), APP_PR, true);
        return saveDBPropsCallable.call().getProperty(ConstantsFor.PR_FORCE).equals("true");
    }

    private Properties getAppProps() {
        threadConfig().thrNameSet("getAPr");
        MysqlDataSource mysqlDataSource = new DBRegProperties(DB_JAVA_ID).getRegSourceForProperties();
        mysqlDataSource.setRelaxAutoCommit(true);
        mysqlDataSource.setLogger("java.util.Logger");
        Callable<Properties> theProphecy = new DBPropsCallable(mysqlDataSource , APP_PR);
        try {
            APP_PR.putAll(theProphecy.call());
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getAppProps", e));
        }
        return APP_PR;
    }
    
    public boolean launchRegRuFTPLibsUploader() {
        Runnable regRuFTPLibsUploader = new RegRuFTPLibsUploader();
        return threadConfig().execByThreadConfig(regRuFTPLibsUploader);
    }
}
