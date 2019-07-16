// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.jcraft.jsch.JSch;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.FilePropsLocal;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.NetScannerSvc;
import ru.vachok.networker.exe.runnabletasks.TemporaryFullInternet;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.exe.schedule.DiapazonScan;
import ru.vachok.networker.exe.schedule.Do0213Networker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.libswork.RegRuFTPLibsUploader;
import ru.vachok.networker.net.scanner.NetListKeeper;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.InitProperties;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.props.DBPropsCallable;
import ru.vachok.networker.services.ADSrv;
import ru.vachok.networker.services.SimpleCalculator;
import ru.vachok.networker.sysinfo.VersionInfo;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringJoiner;
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
    
    private static MessageToUser messageToUser = new MessageLocal(AppComponents.class.getSimpleName());
    
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
        String methName = ".connection";
        DataConnectTo dataConnectTo = new RegRuMysqlLoc();
        return dataConnectTo.getDefaultConnection(dbName);
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
    public static @NotNull Do0213Networker do0213Monitor() {
        return new Do0213Networker("10.200.213.85");
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
    
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public static Properties getProps() {
        try (InputStream inputStream = AppComponents.class.getResourceAsStream("/static/const.properties")) {
            APP_PR.load(inputStream);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("AppComponents.getProps\n{0}: {1}\nParameters: []\nReturn: java.util.Properties\nStack:\n{2}", e.getClass().getTypeName(), e
                    .getMessage(), new TForms().fromArray(e)));
        }
        InitProperties initProperties = new DBPropsCallable();
        APP_PR.putAll(initProperties.getProps());
        if (APP_PR.size() > 3) {
            if ((APP_PR.getProperty(ConstantsFor.PR_DBSTAMP) != null)) {
                long threeHoursAfterUpdateFromDB = Long.parseLong(APP_PR.getProperty(ConstantsFor.PR_DBSTAMP)) + TimeUnit.MINUTES
                    .toMillis((long) (ConstantsFor.ONE_HOUR_IN_MIN * 3));
                if (threeHoursAfterUpdateFromDB < System.currentTimeMillis()) {
                    APP_PR.putAll(initProperties.getProps());
                }
            }
            try {
                APP_PR
                    .store(new FileOutputStream(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES), "ru.vachok.networker.AppComponents.getProps");
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat
                    .format("AppComponents.getProps\n{0}: {1}\nParameters: []\nReturn: java.util.Properties\nStack:\n{2}", e.getClass().getTypeName(), e
                        .getMessage(), new TForms().fromArray(e)));
            }
        }
        else {
            Properties props = initProperties.getProps();
            APP_PR.putAll(props);
            APP_PR.setProperty(ConstantsFor.PR_DBSTAMP, String.valueOf(System.currentTimeMillis()));
            APP_PR.setProperty(ConstantsFor.PR_THISPC, ConstantsFor.thisPC());
            if (APP_PR.size() > 9) {
                initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
                initProperties.delProps();
                initProperties.setProps(APP_PR);
            }
        }
        return APP_PR;
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", AppComponents.class.getSimpleName() + "[\n", "\n]")
            .add("Nothing to show...")
            .toString();
    }
    
    private void checkUptimeForUpdate() {
        InitProperties initProperties = new DBPropsCallable();
        initProperties.delProps();
        initProperties.setProps(APP_PR);
        initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
        initProperties.delProps();
        initProperties.setProps(APP_PR);
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
    
    /**
     @param toUpd {@link Properties}, для хранения в БД
     @deprecated 16.07.2019 (0:29)
     */
    @Deprecated
    private void updateProps(@NotNull Properties toUpd) {
        if (toUpd.size() > 9) {
            APP_PR.clear();
            APP_PR.putAll(toUpd);
            checkUptimeForUpdate();
        }
        else {
            throw new IllegalComponentStateException("Properties to small : " + APP_PR.size());
        }
    }
    
    private void filePropsNoWritable(@NotNull File constForProps) {
        InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
        AppComponents.APP_PR.clear();
        AppComponents.APP_PR.putAll(initProperties.getProps());
    
        messageToUser.info(MessageFormat.format("File {1}. setWritable({0}), changed: {2}, size = {3} bytes. ",
            constForProps.setWritable(true), constForProps.getName(), new Date(constForProps.lastModified()), constForProps.length()));
    
        initProperties = new DBPropsCallable();
    
        boolean isSetToDB = initProperties.setProps(AppComponents.APP_PR);
    
    }
}
