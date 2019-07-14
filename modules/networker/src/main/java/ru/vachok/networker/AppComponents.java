// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.jcraft.jsch.JSch;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.NetScannerSvc;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.exe.schedule.Do0213Monitor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.libswork.RegRuFTPLibsUploader;
import ru.vachok.networker.net.scanner.NetListKeeper;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.services.ADSrv;
import ru.vachok.networker.services.SimpleCalculator;
import ru.vachok.networker.sysinfo.VersionInfo;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 Компоненты. Бины
 
 @see ru.vachok.networker.AppComponentsTest
 @since 02.05.2018 (22:14) */
@SuppressWarnings({"OverlyCoupledClass", "ClassWithTooManyMethods"})
@ComponentScan
public class AppComponents {
    
    
    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_VISITOR = "visitor";
    
    private static final Properties APP_PR = new Properties();
    
    private static final String DB_JAVA_ID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
    
    private static final ThreadConfig THREAD_CONFIG = ThreadConfig.getI();
    
    private static MessageToUser messageToUser = new DBMessenger(AppComponents.class.getSimpleName());
    
    public static @NotNull String ipFlushDNS() throws UnsupportedOperationException {
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
    
    public Connection connection(String dbName) {
        try {
            return new RegRuMysqlLoc().getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
        }
        catch (Exception e) {
            return new RegRuMysql().getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
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
    
    @Scope(ConstantsFor.SINGLETON)
    @Bean
    @Contract(" -> new")
    public static @NotNull Do0213Monitor do0213Monitor() {
        return new Do0213Monitor("10.200.213.85");
    }
    
    @Bean(STR_VISITOR)
    public Visitor visitor(HttpServletRequest request) {
        Visitor visitor = new Visitor(request);
        ExitApp.getVisitsMap().putIfAbsent(request.getSession().getCreationTime(), visitor);
        return visitor;
    }
    
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public SaveLogsToDB saveLogsToDB() {
        return new SaveLogsToDB();
    }
    
    @Contract(pure = true)
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
    
    public static Properties getMailProps() {
        Properties properties = new Properties();
        try {
            properties.load(AppComponents.class.getResourceAsStream("/static/mail.properties"));
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return properties;
    }
    
    /**
     new {@link ADComputer} + new {@link ADUser}
     
     @return new {@link ADSrv}
     */
    @Bean
    public static @NotNull ADSrv adSrv() {
        ADUser adUser = new ADUser();
        ADComputer adComputer = new ADComputer();
        return new ADSrv(adUser, adComputer);
    }
    
    @Contract(" -> fail")
    public static AbstractBeanFactoryBasedTargetSource configurableApplicationContext() {
        throw new IllegalComponentStateException("Moved to: " + IntoApplication.class.getSimpleName());
    }
    
    public boolean updateProps(@NotNull Properties propertiesToUpdate) throws IOException {
        if (propertiesToUpdate.size() > 5) {
            File constantsForProps = new File(ConstantsFor.PROPS_FILE_JAVA_ID);
            System.out.println("constantsForProps.setWritable(true) = " + constantsForProps.setWritable(true));
            System.out.println("constantsForProps.delete() = " + constantsForProps.delete());
            propertiesToUpdate.store(new FileOutputStream(ConstantsFor.PROPS_FILE_JAVA_ID), getClass().getSimpleName() + ".updateProps");
        }
        int updTable = new DBPropsCallable(propertiesToUpdate).updateTable();
        return updTable > 0;
    }
    
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public static Properties getProps() {
        File fileProps = new File(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES);
        
        if (APP_PR.size() > 3) {
            if ((APP_PR.getProperty(ConstantsFor.PR_DBSTAMP) != null) && (Long.parseLong(APP_PR.getProperty(ConstantsFor.PR_DBSTAMP)) + TimeUnit.MINUTES.toMillis(180)) < System
                .currentTimeMillis()) {
                APP_PR.putAll(new AppComponents().getAppProps());
            }
            if (!fileProps.canWrite()) {
                new AppComponents().filePropsNoWritable(fileProps);
            }
        }
        else {
            APP_PR.putAll(new AppComponents().getAppProps());
            APP_PR.setProperty(ConstantsFor.PR_DBSTAMP, String.valueOf(System.currentTimeMillis()));
            APP_PR.setProperty(ConstantsFor.PR_THISPC, ConstantsFor.thisPC());
        }
        return APP_PR;
    }
    
    public void updateProps() throws IOException {
        if (APP_PR.size() > 3) {
            updateProps(APP_PR);
        }
        else {
            throw new IllegalComponentStateException("Properties to small : " + APP_PR.size());
        }
    }
    
    public static String diapazonedScanInfo() {
        return DiapazonScan.getInstance().theInfoToString();
    }
    
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }
    
    public ScanOnline scanOnline() {
        return new ScanOnline();
    }
    
    public PfLists getPFLists() {
        return new PfLists();
    }
    
    public static Preferences getUserPref() {
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(FileSystemWorker.error(AppComponents.class.getSimpleName() + ".getUserPref", e));
        }
        return preferences;
    }
    
    @Contract(pure = true)
    @Scope(ConstantsFor.SINGLETON)
    public static NetListKeeper netKeeper() {
        return NetListKeeper.getI();
    }
    
    @Contract("_ -> new")
    public static @NotNull VersionInfo versionInfo(String pcName) {
        return new VersionInfo(getProps(), pcName);
    }
    
    /**
     @return new {@link VersionInfo}
     */
    @Scope(ConstantsFor.SINGLETON)
    @Bean(ConstantsFor.STR_VERSIONINFO)
    @Contract(" -> new")
    static @NotNull VersionInfo versionInfo() {
        return new VersionInfo(APP_PR, ConstantsFor.thisPC());
    }
    
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    TemporaryFullInternet temporaryFullInternet() {
        return new TemporaryFullInternet();
    }
    
    String launchRegRuFTPLibsUploader() {
        Runnable regRuFTPLibsUploader = new RegRuFTPLibsUploader();
//        Callable<String> coverReportUpdate = new CoverReportUpdate();
        try {
            boolean isExec = threadConfig().execByThreadConfig(regRuFTPLibsUploader);
//            Future<String> submit = threadConfig().getTaskExecutor().submit(coverReportUpdate);
//            String coverReportUpdateFutureStr = submit.get();
            return String.valueOf(true);
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }
    
    private void filePropsNoWritable(@NotNull File constForProps) {
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        AppComponents.APP_PR.clear();
        AppComponents.APP_PR.putAll(initProperties.getProps());
        System.out.println("constForProps.setWritable(true) = " + constForProps.setWritable(true));
        initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
        initProperties.delProps();
        initProperties.setProps(AppComponents.APP_PR);
    }
    
    private Properties getAppProps() {
        Callable<Properties> theProphecy = new DBPropsCallable(APP_PR);
        try {
            APP_PR.putAll(theProphecy.call());
        }
        catch (Exception e) {
            messageToUser.error(e.getMessage());
        }
        return APP_PR;
    }
}
