// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.componentsrepo.services.RegRuFTPLibsUploader;
import ru.vachok.networker.componentsrepo.services.SimpleCalculator;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.monitor.PCMonitoring;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.net.ssh.PfLists;
import ru.vachok.networker.net.ssh.SshActs;
import ru.vachok.networker.net.ssh.TemporaryFullInternet;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.DBPropsCallable;
import ru.vachok.networker.restapi.props.FilePropsLocal;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.sysinfo.VersionInfo;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Properties;
import java.util.StringJoiner;
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
    
    private static final ThreadConfig THREAD_CONFIG = ThreadConfig.getI();
    
    private static MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, AppComponents.class.getSimpleName());
    
    public static @NotNull Properties getMailProps() {
        Properties properties = new Properties();
        try {
            properties.load(AppComponents.class.getResourceAsStream("/static/mail.properties"));
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return properties;
    }
    
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public static Properties getProps() {
        boolean isSmallSize = APP_PR.size() < 9;
        if (isSmallSize) {
            loadPropsFromDB();
            if (isSmallSize) {
                APP_PR.putAll(new DBPropsCallable().call());
            }
            if (!isSmallSize) {
                InitProperties.getInstance(InitProperties.FILE).setProps(APP_PR);
            }
            else {
                messageToUser.error(AppComponents.class.getSimpleName(), "APP_PR getter. line 135", MessageFormat.format(" size = {0} !!!", APP_PR.size()));
            }
            return APP_PR;
        }
        else {
            return APP_PR;
        }
    }
    
    public AppComponents() {
        InitProperties initProperties = InitProperties.getInstance(InitProperties.DB);
        if (APP_PR.isEmpty()) {
            APP_PR.putAll(initProperties.getProps());
        }
    }
    
    public PfLists getPFLists() {
        return new PfLists();
    }
    
    @Scope(ConstantsFor.SINGLETON)
    public static Preferences getUserPref() {
        Preferences prefsNeededNode = prefsNeededNode();
        try {
            prefsNeededNode.flush();
            prefsNeededNode.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(MessageFormat.format("AppComponents.getUserPref: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return prefsNeededNode;
    }
    
    @Contract(value = " -> new", pure = true)
    @Bean
    public static @NotNull ADSrv adSrv() {
        return new ADSrv();
    }
    
    @Contract(pure = true)
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static ThreadConfig threadConfig() {
        return THREAD_CONFIG;
    }
    
    public Connection connection(String dbName) {
        MysqlDataSource mysqlDataSource = DataConnectTo.getRemoteReg().getDataSource();
        Properties properties = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
        mysqlDataSource.setUser(properties.getProperty(PropertiesNames.DBUSER));
        mysqlDataSource.setPassword(properties.getProperty(PropertiesNames.DBPASS));
        mysqlDataSource.setDatabaseName(dbName);
        try {
            return mysqlDataSource.getConnection();
        }
        catch (SQLException | ArrayIndexOutOfBoundsException e) {
            return DataConnectToAdapter.getRegRuMysqlLibConnection(dbName);
        }
    }
    
    @Contract(value = "_ -> new", pure = true)
    @Scope(ConstantsFor.SINGLETON)
    public static @NotNull MessageSwing getMessageSwing(String messengerHeader) {
//        final MessageSwing messageSwing = new ru.vachok.networker.restapi.message.MessageSwing( frameWidth , frameHeight);
        return new ru.vachok.messenger.MessageSwing();
    }
    
    public static @NotNull NetScanService onePCMonStart() {
        NetScanService do0055 = new PCMonitoring("do0055", (LocalTime.parse("17:30").toSecondOfDay() - LocalTime.now().toSecondOfDay()));
        boolean isAfter830 = LocalTime.parse("08:30").toSecondOfDay() < LocalTime.now().toSecondOfDay();
        boolean isBefore1730 = LocalTime.now().toSecondOfDay() < LocalTime.parse("17:30").toSecondOfDay();
        boolean isWeekEnds = (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY) || LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY));
        
        if (!isWeekEnds && isAfter830 && isBefore1730) {
            threadConfig().execByThreadConfig(do0055);
            threadConfig().getTaskScheduler().schedule(do0055, MyCalen.getNextDay(8, 30));
        }
        return do0055;
    }
    
    /**
     @return new {@link SimpleCalculator}
     */
    @Bean(ConstantsFor.BEANNAME_CALCULATOR)
    public SimpleCalculator simpleCalculator() {
        return new SimpleCalculator();
    }
    
    @Bean(STR_VISITOR)
    public Visitor visitor(HttpServletRequest request) {
        Visitor visitor = new Visitor(request);
        ExitApp.getVisitsMap().putIfAbsent(request.getSession().getCreationTime(), visitor);
        return visitor;
    }
    
    public NetScanService scanOnline() {
        return new ScanOnline();
    }
    
    private static void loadPropsFromDB() {
        InitProperties initProperties = InitProperties.getInstance(InitProperties.DB);
        Properties props = initProperties.getProps();
        APP_PR.putAll(props);
        APP_PR.setProperty(PropertiesNames.PR_DBSTAMP, String.valueOf(System.currentTimeMillis()));
        APP_PR.setProperty(PropertiesNames.PR_THISPC, UsefulUtilities.thisPC());
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection("test.test")) {
            messageToUser.warn(String.valueOf(connection.isValid(4)));
        }
        catch (SQLException ignore) {
            //03.10.2019 (12:27)
        }
    }
    
    private static Preferences prefsNeededNode() {
        Preferences nodeNetworker = Preferences.userRoot().node(ConstantsFor.PREF_NODE_NAME);
        try {
            nodeNetworker.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(FileSystemWorker.error(AppComponents.class.getSimpleName() + ".getUserPref", e));
        }
        return nodeNetworker;
    }
    
    public SshActs sshActs() {
        return new SshActs();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", AppComponents.class.getSimpleName() + "[\n", "\n]")
            .add("Nothing to show...")
            .toString();
    }
    
    /**
     @return new {@link VersionInfo}
     */
    @Scope(ConstantsFor.SINGLETON)
    @Bean(ConstantsFor.STR_VERSIONINFO)
    @Contract(" -> new")
    static @NotNull VersionInfo versionInfo() {
        return new VersionInfo(APP_PR, UsefulUtilities.thisPC());
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
    
    private void checkUptimeForUpdate() {
        InitProperties initProperties = new DBPropsCallable();
        initProperties.delProps();
        initProperties.setProps(APP_PR);
        initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
        initProperties.delProps();
        initProperties.setProps(APP_PR);
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
