package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.networker.logic.CookTheCookie;
import ru.vachok.networker.logic.DBMessenger;
import ru.vachok.networker.services.*;

import javax.servlet.http.HttpServletRequest;


@ComponentScan
public class AppComponents {

    @Bean
    public static Logger getLogger() {
        return LoggerFactory.getLogger("ru_vachok_networker");
    }

    @Bean
    public static ADSrv adSrv() {
        ADUser adUser = new ADUser();
        ADComputer adComputer = new ADComputer();
        return new ADSrv(adUser, adComputer);
    }

    @Bean
    @Scope ("singleton")
    public DBMessenger dbMessenger() {
        return new DBMessenger();
    }

    @Bean()
    @Scope ("singleton")
    public NetScannerSvc netScan() {
        return new NetScannerSvc();
    }

    @Bean
    @Scope("singleton")
    public DataBasesSRV dataBases() {
        return new DataBasesSRV();
    }

    @Bean("pflists")
    @Scope("singleton")
    public PfLists pfLists() {
        return new PfLists();
    }

    @Bean("visitor")
    @Scope("prototype")
    public Visitor visitor(HttpServletRequest request) {
        return new Visitor(request);
    }

    @Bean
    @Scope("singleton")
    public VisitorSrv visitorSrv(CookieShower cookieShower, Visitor visitor) {
        return new VisitorSrv(cookieShower, visitor);
    }

    @Bean
    public WhoIsWithSRV whoIsWithSRV() {
        return new WhoIsWithSRV();
    }

    @Bean("versioninfo")
    public VersionInfo versionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setTimeStamp(System.currentTimeMillis() + "");
        return versionInfo;
    }

    public PfListsSrv pfListsSrv(PfLists pfLists) {
        return new PfListsSrv(pfLists);
    }

    @Bean
    @Scope("prototype")
    public CookTheCookie cookTheCookie(Visitor visitor) {
        return new CookTheCookie(visitor);
    }
}
