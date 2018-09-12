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
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.logic.DBMessenger;
import ru.vachok.networker.services.PfListsSrv;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> new Visitor().shutdownHook()));
        appCtx.registerShutdownHook();
    }

    private static void infoForU(ApplicationContext appCtx) {
        scheduleAns();
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

    public static void scheduleAns() {
        ScheduledExecutorService executorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        Runnable runnable = () -> {
            float upTime = (float) (System.currentTimeMillis() - ConstantsFor.START_STAMP) /
                TimeUnit.DAYS.toMillis(1);
            new PfListsSrv();
            String msg = upTime + " uptime days";
            LOGGER.info(msg);
        };
        int delay = new Random().nextInt((int) TimeUnit.MINUTES.toSeconds(17) / 3);
        int init = new Random().nextInt((int) TimeUnit.MINUTES.toSeconds(20));
        executorService.scheduleWithFixedDelay(runnable, init, delay, TimeUnit.SECONDS);
        String msg = executorService.toString() + " " + init + " init ," + delay + " delay";
        LOGGER.info(msg);
    }

}