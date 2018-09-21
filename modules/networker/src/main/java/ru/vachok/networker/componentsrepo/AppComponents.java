package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.logic.DBMessenger;
import ru.vachok.networker.services.DataBasesSRV;
import ru.vachok.networker.services.NetScannerSvc;
import ru.vachok.networker.services.VisitorSrv;
import ru.vachok.networker.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;


@ComponentScan
public class AppComponents {

    @Bean
    public static Logger getLogger() {
        return LoggerFactory.getLogger("ru_vachok_networker");
    }

    @Bean()
    @Scope ("prototype")
    public ADUser adUser() {
        ADUser adUser = new ADUser();
        return adUser;
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
        AnnotationConfigApplicationContext appCtx = IntoApplication.getAppCtx();
        VisitorSrv visitorSrv = appCtx.getBean(VisitorSrv.class);
        return visitorSrv.makeVisit(request);
    }

    @Bean
    @Scope("singleton")
    public VisitorSrv visitorSrv() {
        return new VisitorSrv();
    }

    @Bean
    public WhoIsWithSRV whoIsWithSRV() {
        return new WhoIsWithSRV();
    }
}
