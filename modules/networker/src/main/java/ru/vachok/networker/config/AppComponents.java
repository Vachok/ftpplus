package ru.vachok.networker.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.networker.beans.ADUser;
import ru.vachok.networker.beans.DBMessenger;
import ru.vachok.networker.beans.NetScannerSvc;


@ComponentScan
public class AppComponents {

    @Bean
    public static Logger logger() {
        return LoggerFactory.getLogger("ru_vachok_networker");
    }

    @Bean (initMethod = "initUser", destroyMethod = "rmUser")
    @Scope ("prototype")
    public ADUser adUser() {
        return new ADUser();
    }

    @Bean (destroyMethod = "dbSend")
    @Scope ("singleton")
    public DBMessenger dbMessenger() {
        return new DBMessenger();
    }

    @Bean (initMethod = "netSvc")
    @Scope ("singleton")
    public NetScannerSvc netScan(String queryString) {
        return new NetScannerSvc();
    }
}
