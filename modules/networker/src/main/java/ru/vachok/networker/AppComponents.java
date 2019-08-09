// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.jcraft.jsch.JSch;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.accesscontrol.sshactions.TemporaryFullInternet;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.PCUserResolver;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.exceptions.PropertiesAppNotFoundException;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.net.libswork.RegRuFTPLibsUploader;
import ru.vachok.networker.net.monitor.DiapazonScan;
import ru.vachok.networker.net.monitor.PCMonitoring;
import ru.vachok.networker.net.scanner.NetScannerSvc;
import ru.vachok.networker.net.scanner.ScanOnline;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.props.DBPropsCallable;
import ru.vachok.networker.restapi.props.FilePropsLocal;
import ru.vachok.networker.restapi.props.InitProperties;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.SimpleCalculator;
import ru.vachok.networker.sysinfo.VersionInfo;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
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
    
    public AppComponents() {
        if (APP_PR.isEmpty()) {
            loadPropsAndWriteToFile();
        }
    }
    
    /**
     @return ipconfig /flushdns results from console
     
     @throws UnsupportedOperationException if non Windows OS
     @see ru.vachok.networker.AppComponentsTest#testIpFlushDNS
     */
    public static @NotNull String ipFlushDNS() {
        if (System.getProperty("os.name").toLowerCase().contains(PropertiesNames.PR_WINDOWSOS)) {
            try {
                return runProcess();
            }
            catch (IOException e) {
                return e.getMessage();
            }
        }
        else {
            return System.getProperty("os.name");
        }
    }
    
    public Connection connection(String dbName) throws SQLException {
        MysqlDataSource mysqlDataSource = DataConnectToAdapter.getLibDataSource();
        Properties properties = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
        StringBuilder stringBuilder = new StringBuilder();
        mysqlDataSource.setUser(properties.getProperty(PropertiesNames.PR_DBUSER));
        mysqlDataSource.setPassword(properties.getProperty(PropertiesNames.PR_DBPASS));
        mysqlDataSource.setDatabaseName(dbName);
        mysqlDataSource.setEncoding("UTF-8");
        mysqlDataSource.setCharacterEncoding("UTF-8");
        mysqlDataSource.setAutoReconnect(true);
        mysqlDataSource.setLoginTimeout(30);
        mysqlDataSource.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30));
        try {
            return mysqlDataSource.getConnection();
        }
        catch (Exception e) {
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
        return new NetScannerSvc();
    }
    
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
    
    @Bean
    public static @NotNull ADSrv adSrv() {
        ADUser adUser = new ADUser();
        ADComputer adComputer = new ADComputer();
        return new ADSrv(adUser, adComputer);
    }
    
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public static Properties getProps() throws PropertiesAppNotFoundException {
        if (APP_PR.isEmpty()) {
            loadPropsFromDB();
            return APP_PR;
        }
        else {
            return APP_PR;
        }
    }
    
    public static String diapazonedScanInfo() {
        return DiapazonScan.getInstance().getPingResultStr();
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
            preferences.flush();
            preferences.sync();
            preferences.exportNode(new FileOutputStream(preferences.name() + ".prefer"));
        }
        catch (IOException | BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
        return preferences;
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
    
    protected static Preferences prefsNeededNode() {
        Preferences nodeNetworker = Preferences.userRoot().node(ConstantsFor.PREF_NODE_NAME);
        try {
            nodeNetworker.flush();
            nodeNetworker.sync();
            nodeNetworker.exportNode(new FileOutputStream(nodeNetworker.name() + ".prefs"));
        }
        catch (BackingStoreException | IOException e) {
            messageToUser.error(FileSystemWorker.error(AppComponents.class.getSimpleName() + ".getUserPref", e));
        }
        return nodeNetworker;
    }
    
    public PCUserResolver getUserResolver(String pcName) {
        return new PCUserResolver(pcName);
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
    
    protected void loadPropsAndWriteToFile() {
        InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
        //noinspection MagicNumber
        if (APP_PR.size() > 12) {
            initProperties.setProps(APP_PR);
        }
        else {
            loadPropsFromDB();
        }
        if (APP_PR.size() < 9) {
            throw new PropertiesAppNotFoundException(APP_PR.size());
        }
    }
    
    private static @NotNull String runProcess() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Process processFlushDNS = Runtime.getRuntime().exec("ipconfig /flushdns");
        InputStream flushDNSInputStream = processFlushDNS.getInputStream();
        InputStreamReader reader = new InputStreamReader(flushDNSInputStream);
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines().forEach(stringBuilder::append);
        }
        return stringBuilder.toString();
    }
    
    private static void loadPropsFromDB() {
        Properties props = new DBPropsCallable(ConstantsFor.APPNAME_WITHMINUS, ConstantsFor.class.getSimpleName()).call();
        APP_PR.putAll(props);
        APP_PR.setProperty(PropertiesNames.PR_DBSTAMP, String.valueOf(System.currentTimeMillis()));
        APP_PR.setProperty(PropertiesNames.PR_THISPC, UsefulUtilities.thisPC());
    }
    
    private static void loadInsideJAR() {
        try (InputStream inputStream = AppComponents.class.getResourceAsStream(ConstantsFor.STREAMJAR_PROPERTIES)) {
            APP_PR.load(inputStream);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("AppComponents.getProps\n{0}: {1}\nParameters: []\nReturn: java.util.Properties\nStack:\n{2}", e.getClass().getTypeName(), e
                    .getMessage(), new TForms().fromArray(e)));
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
