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
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.NetScannerSvc;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.libswork.RegRuFTPLibsUploader;
import ru.vachok.networker.services.ADSrv;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.SimpleCalculator;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 Компоненты. Бины
 
 @since 02.05.2018 (22:14) */
@SuppressWarnings("OverlyCoupledClass") @ComponentScan
public class AppComponents {
    
    
    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_VISITOR = "visitor";
    
    private static final Properties APP_PR = new Properties();
    
    private static final String DB_JAVA_ID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
    
    private static final ThreadConfig THREAD_CONFIG = ThreadConfig.getI();
    
    private static MessageToUser messageToUser = new MessageLocal(AppComponents.class.getSimpleName());
    
    public static String ipFlushDNS() throws UnsupportedOperationException {
        StringBuilder stringBuilder = new StringBuilder();
        if (System.getProperty("os.name").toLowerCase().contains(ConstantsFor.PR_WINDOWSOS)) {
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
        }
        return stringBuilder.toString();
    }
    
    @Bean
    public Connection connection(String dbName) throws IOException {
        
        try {
            MysqlDataSource dataSource = new RegRuMysql().getDataSourceSchema(dbName);
            
            dataSource.setAutoReconnect(true);
            dataSource.setRelaxAutoCommit(true);
            dataSource.setInteractiveClient(true);
            return dataSource.getConnection();
        }
        catch (SQLException e) {
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
        return new SshActs();
    }
    
    @Bean(STR_VISITOR)
    public Visitor visitor(HttpServletRequest request) {
        Visitor visitor = new Visitor(request);
        return ExitApp.getVisitsMap().putIfAbsent(request.getSession().getCreationTime(), visitor);
    }
    
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public SaveLogsToDB saveLogsToDB() {
        SaveLogsToDB.startScheduled();
        return SaveLogsToDB.getI();
    }
    
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static ThreadConfig threadConfig() {
        return THREAD_CONFIG;
    }
    
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static NetScannerSvc netScannerSvc() {
        return NetScannerSvc.getInst();
    }
    
    /**
     @return new {@link VersionInfo}
     */
    @Bean(ConstantsFor.STR_VERSIONINFO)
    @Scope(ConstantsFor.SINGLETON)
    public static VersionInfo versionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        if (ConstantsFor.thisPC().toLowerCase().contains("home") | ConstantsFor.thisPC().toLowerCase().contains("do00213")) {
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
        if (propertiesToUpdate.size() > 5) {
            File pFile = new File(ConstantsFor.PROPS_FILE_JAVA_ID);
            pFile.setWritable(true);
            pFile.delete();
            propertiesToUpdate.store(new FileOutputStream(ConstantsFor.PROPS_FILE_JAVA_ID), getClass().getSimpleName() + ".updateProps");
        }
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
        boolean isDel = initProperties.delProps();
        boolean isSet = initProperties.setProps(propertiesToUpdate);
        return isDel & isSet;
    }
    
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") public static Properties getProps() {
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
    
    public static String diapazonedScan() {
        return DiapazonScan.getInstance().theInfoToString();
    }
    
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }
    
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    TemporaryFullInternet temporaryFullInternet() {
        return new TemporaryFullInternet();
    }
    
    boolean launchRegRuFTPLibsUploader() {
        Runnable regRuFTPLibsUploader = new RegRuFTPLibsUploader();
        try {
            return threadConfig().execByThreadConfig(regRuFTPLibsUploader);
        }
        catch (Exception e) {
            messageToUser.error(e.getMessage());
            return false;
        }
    }
    
    private Properties getAppProps() {
        threadConfig().thrNameSet("getAPr");
        MysqlDataSource mysqlDataSource = new DBRegProperties(DB_JAVA_ID).getRegSourceForProperties();
        mysqlDataSource.setRelaxAutoCommit(true);
        mysqlDataSource.setLogger("java.util.Logger");
        Callable<Properties> theProphecy = new DBPropsCallable(mysqlDataSource, APP_PR);
        try {
            APP_PR.putAll(theProphecy.call());
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getAppProps", e));
        }
        return APP_PR;
    }
}
