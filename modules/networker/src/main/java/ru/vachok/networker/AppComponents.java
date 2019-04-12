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
import ru.vachok.networker.accesscontrol.SshActs;
import ru.vachok.networker.accesscontrol.TemporaryFullInternet;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.SimpleCalculator;
import ru.vachok.stats.SaveLogsToDB;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Callable;
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

    private static MessageToUser messageToUser = new MessageLocal(AppComponents.class.getSimpleName());

    private static final Properties APP_PR = new Properties();

    private static final ConcurrentMap<Long, Visitor> VISITS_MAP = new ConcurrentHashMap<>();


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

    private static final String DB_JAVA_ID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
    
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
        if (!ConstantsFor.thisPC().toLowerCase().contains("rups00")) versionInfo.setParams();
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
        throw new IllegalComponentStateException("Moved to");
    }
    
    public boolean updateProps(Properties propertiesToUpdate) {
        MysqlDataSource source = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName()).getRegSourceForProperties();
        DBPropsCallable dbPropsCallable = new DBPropsCallable(source, propertiesToUpdate, true);
        return dbPropsCallable.call().getProperty(ConstantsFor.PR_FORCE).equals("true");
    }


    public static Properties getOrSetProps() {
        if (APP_PR.size() > 3) {
            return APP_PR;
        }
        else {
            return new AppComponents().getAppProps();
        }
    }


    private static boolean saveAppPropsForce() {
        DBPropsCallable saveDBPropsCallable = new DBPropsCallable(new DBRegProperties(DB_JAVA_ID).getRegSourceForProperties(), APP_PR, true);
        return saveDBPropsCallable.call().getProperty(ConstantsFor.PR_FORCE).equals("true");
    }

    private Properties getAppProps() {
        threadConfig().thrNameSet("getAPr");
        if (APP_PR.size() > 3) return APP_PR;
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
}
