// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.google.firebase.FirebaseApp;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.FakeRequest;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.services.SimpleCalculator;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.firebase.FBAdmin;
import ru.vachok.networker.net.ssh.PfLists;
import ru.vachok.networker.net.ssh.SshActs;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.FilePropsLocal;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringJoiner;


/**
 Компоненты. Бины

 @see ru.vachok.networker.AppComponentsTest
 @since 02.05.2018 (22:14) */
@ComponentScan
public class AppComponents {


    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_VISITOR = "visitor";

    /**
     IMPORTANT FOR FIREBASE SYNC
     */
    private static final FBAdmin fbAdmin = new FBAdmin();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, AppComponents.class.getSimpleName());

    private static final AppConfigurationLocal THREAD_CONFIG = ThreadConfig.getI();

    @NotNull
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

    public PfLists getPFLists() {
        return new PfLists();
    }

    @Bean(value = "fbapp")
    public static FirebaseApp getFirebaseApp() {
        return FirebaseApp.getInstance();
    }

    @Contract(value = "_ -> new", pure = true)
    @Scope(ConstantsFor.SINGLETON)
    @NotNull
    public static MessageSwing getMessageSwing(String messengerHeader) {
//        final MessageSwing messageSwing = new ru.vachok.networker.restapi.message.MessageSwing( frameWidth , frameHeight);
        return new ru.vachok.messenger.MessageSwing(messengerHeader);
    }

    public Connection connection(String dbName) {
        MysqlDataSource mysqlDataSource = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDataSource();
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
    public Visitor visitor(@NotNull HttpServletRequest request) {
        if (request.getSession() == null) {
            request = new FakeRequest();
        }
        Visitor visitor = new Visitor(request);
        ExitApp.getVisitsMap().putIfAbsent(request.getSession().getCreationTime(), visitor);
        return visitor;
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

    @Contract(value = " -> new", pure = true)
    @Bean
    @NotNull
    public static ADSrv adSrv() {
        return new ADSrv();
    }

    @Contract(pure = true)
    public static ThreadConfig threadConfig() {
        return (ThreadConfig) THREAD_CONFIG;
    }
}
