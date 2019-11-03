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
import ru.vachok.networker.componentsrepo.services.RegRuFTPLibsUploader;
import ru.vachok.networker.componentsrepo.services.SimpleCalculator;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.scanner.PcNamesScanner;
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
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 Компоненты. Бины
 
 @see ru.vachok.networker.AppComponentsTest
 @since 02.05.2018 (22:14) */
@SuppressWarnings({"OverlyCoupledClass"})
@ComponentScan
public class AppComponents {
    
    
    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_VISITOR = "visitor";
    
    private static final Properties APP_PR = new Properties();
    
    private static final ThreadConfig THREAD_CONFIG = ThreadConfig.getI();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, AppComponents.class.getSimpleName());
    
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
            isSmallSize = APP_PR.size() < 9;
            if (isSmallSize) {
                APP_PR.putAll(new DBPropsCallable().call());
                isSmallSize = APP_PR.size() < 9;
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
    
    @Scope(ConstantsFor.SINGLETON)
    public static Preferences getUserPref() {
        Preferences prefsNeededNode = Preferences.userRoot();
        try {
            prefsNeededNode.flush();
            prefsNeededNode.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(MessageFormat.format("AppComponents.getUserPref: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return prefsNeededNode;
    }
    
    @Contract(pure = true)
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static @NotNull PcNamesScanner getPcNamesScanner() { //todo
        //noinspection UnnecessaryLocalVariable test 26.10.2019 (2:21)
        final PcNamesScanner pcNamesScanner = new PcNamesScanner();
        return pcNamesScanner;
    }
    
    public PfLists getPFLists() {
        return new PfLists();
    }
    
    @Contract(pure = true)
    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static ThreadConfig threadConfig() {
        return THREAD_CONFIG;
    }
    
    public AppComponents() {
        InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
        if (APP_PR.isEmpty()) {
            APP_PR.putAll(initProperties.getProps());
        }
    }
    
    @Contract(value = "_ -> new", pure = true)
    @Scope(ConstantsFor.SINGLETON)
    public static @NotNull MessageSwing getMessageSwing(String messengerHeader) {
//        final MessageSwing messageSwing = new ru.vachok.networker.restapi.message.MessageSwing( frameWidth , frameHeight);
        return new ru.vachok.messenger.MessageSwing();
    }
    
    @Contract(value = " -> new", pure = true)
    @Bean
    public static @NotNull ADSrv adSrv() {
        return new ADSrv();
    }
    
    public Connection connection(String dbName) {
        MysqlDataSource mysqlDataSource = DataConnectTo.getDefaultI().getDataSource();
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
    
    private static void loadPropsFromDB() {
        InitProperties initProperties;
        try {
            initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
        }
        catch (RuntimeException e) {
            initProperties = InitProperties.getInstance(InitProperties.FILE);
        }
        Properties props = initProperties.getProps();
        APP_PR.putAll(props);
        APP_PR.setProperty(PropertiesNames.DBSTAMP, String.valueOf(System.currentTimeMillis()));
        APP_PR.setProperty(PropertiesNames.THISPC, UsefulUtilities.thisPC());
    }
}
