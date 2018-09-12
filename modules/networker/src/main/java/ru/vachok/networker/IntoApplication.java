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
import ru.vachok.networker.beans.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.logic.StringFromArr;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 The type Inetor application.
 */
@EnableAutoConfiguration
@SpringBootApplication
@EnableScheduling
public class IntoApplication {

    /*Fields*/
    private static final MessageToUser DB_MSG = new DBMessenger();

    private static final String SOURCE_CLASS = IntoApplication.class.getSimpleName();

    private static final Logger LOGGER = AppComponents.getLogger();

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
        appCtx.refresh();
        SpringApplication.run(IntoApplication.class, args);
        infoForU(appCtx);
    }

    private static void infoForU(ApplicationContext appCtx) {
        float hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - ConstantsFor.START_STAMP);
        String s2 = +hours + " h\n" + "Started at " +
            new Date(ConstantsFor.START_STAMP).toString() + "\n" + ConstantsFor.THIS_PC_NAME;
        DB_MSG.info(SOURCE_CLASS, "INFO", s2);
        String msg = new Date(appCtx.getStartupDate()) + new String(" Я РОДИЛСЯ".getBytes(), StandardCharsets.UTF_8);
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
            DB_MSG.errorAlert(IntoApplication.class.getSimpleName(), e.getMessage(), new StringFromArr().fromArr(e.getStackTrace()));
            return false;
        }
    }
}