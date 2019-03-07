package ru.vachok.networker;


import com.jcraft.jsch.JSch;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.accesscontrol.SshActs;
import ru.vachok.networker.accesscontrol.TemporaryFullInternet;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.LastNetScan;
import ru.vachok.networker.componentsrepo.VersionInfo;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.SimpleCalculator;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
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

    @Bean
    public TemporaryFullInternet temporaryFullInternet() {
        TemporaryFullInternet temporaryFullInternet = new TemporaryFullInternet();
        messageToUser.info("AppComponents.temporaryFullInternet", "temporaryFullInternet.hashCode()", " = " + temporaryFullInternet.hashCode());
        return temporaryFullInternet;
    }

    private static MessageToUser messageToUser = new MessageLocal();

    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static Properties getOrSetProps() {
        return getOrSetProps(false);
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
        return new Visitor(request);
    }

    @Bean
    @Scope(ConstantsFor.SINGLETON)
    public static Properties getOrSetProps(boolean saveThis) {
        Properties properties = ConstantsFor.getAppProps();
        if (saveThis) {
            boolean isSaved = ConstantsFor.saveAppProps(properties);
            messageToUser.info("AppComponents. Saving properties", " properties.size()", " = " + properties.size());
            final String classMeth = "AppComponents.getOrSetProps ";
            final String isSavedStr = " isSaved";
            if (isSaved) {
                messageToUser.info(classMeth, isSavedStr, " = " + true);
            } else {
                messageToUser.error(classMeth, isSavedStr, " = " + false);
            }
        }
        return properties;
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
    @Scope(ConstantsFor.SINGLETON)
    public static LastNetScan lastNetScan() {
        return LastNetScan.getLastNetScan();
    }

    /**
     @return new {@link VersionInfo}
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    @Bean("versioninfo")
    @Scope(ConstantsFor.SINGLETON)
    public static VersionInfo versionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        boolean isBUGged = false;
        if (new File("bugged").exists()) {
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
    
    public static boolean getOrSetProps(Properties localProps) {
        return ConstantsFor.saveAppProps(localProps);
    }
    
    @Override
    public String toString() {
        ConfigurableApplicationContext context = getConfigurableApplicationContext();
        final StringBuilder sb = new StringBuilder("AppComponents{");
        sb.append("Beans=").append(new TForms().fromArray(context.getBeanDefinitionNames(), true)).append("\n");
        sb.append(context);
        sb.append('}');
        return sb.toString();
    }
}
