package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.config.ResLoader;
import ru.vachok.networker.logic.DBMessenger;
import ru.vachok.networker.services.PfListsSrv;

import javax.servlet.http.HttpServletRequest;
import java.rmi.UnexpectedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;


/**
 The type Inetor application.
 */
@EnableAutoConfiguration
@SpringBootApplication
@EnableScheduling
public class IntoApplication {

    /*Fields*/
    private static final MessageToUser DB_MSG = new DBMessenger();

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final SpringApplication SPRING_APPLICATION = new SpringApplication();

    private static AnnotationConfigApplicationContext appCtx = AppCtx.scanForBeansAndRefreshContext();

    public static AnnotationConfigApplicationContext getAppCtx() {
        return appCtx;
    }
    /*PS Methods*/

    /**
     The entry point of application.
     <a href="https://goo.gl/K93z2L">APP Engine</a>

     @param args the input arguments
     */
    public static void main(String[] args) {
        SPRING_APPLICATION.setMainApplicationClass(IntoApplication.class);
        SPRING_APPLICATION.setApplicationContextClass(AppCtx.class);
        SPRING_APPLICATION.setResourceLoader(new ResLoader());
        SpringApplication.run(IntoApplication.class, args);
        PfListsSrv pfListsSrv = appCtx.getBean(PfListsSrv.class);
        try{
            pfListsSrv.buildFactory();
        }
        catch(UnexpectedException e){
            LOGGER.error(e.getMessage(), e);
        }
        infoForU(appCtx);
    }

    private static void infoForU(ApplicationContext appCtx) {
        String msg = appCtx.getApplicationName() + " app name" + appCtx.getDisplayName() + " app display name\n";
        LOGGER.info(msg);
    }

    public static boolean dataSender(HttpServletRequest request, String srcClass) {
        String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue) values (?,?,?)";
        try(Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "webapp");
            PreparedStatement p = c.prepareStatement(sql)){
            p.setString(1, srcClass);
            p.setString(2, "request");
            p.setString(3, request.getRemoteHost() +
                "\n" + request.getRemoteAddr() +
                "\n" + Arrays.toString(request.getCookies()) +
                "\n" + request.getServletContext().getServerInfo());
            p.executeUpdate();
            return true;
        }
        catch(SQLException e){
            DB_MSG.errorAlert(IntoApplication.class.getSimpleName(), e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            return false;
        }
    }
}