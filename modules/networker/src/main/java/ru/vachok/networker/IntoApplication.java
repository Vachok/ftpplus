package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.logic.DBMessenger;
import ru.vachok.networker.services.PfListsSrv;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
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

    private static void scheduleAns() {
        TaskExecutor executor = new ThreadConfig().threadPoolTaskExecutor();
        Runnable runnable = () -> {
            Thread.currentThread().setName("id " + System.currentTimeMillis());
            float upTime = (float) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / TimeUnit.DAYS.toMillis(1);
            PfListsSrv.buildFactory();
            String msg = upTime +
                " uptime days. Active threads = " +
                Thread.activeCount() + ". This thread = " +
                Thread.currentThread().getName() + "|" +
                System.currentTimeMillis() + "\n";
            LOGGER.warn(msg);
            Thread.currentThread().interrupt();
        };
        int delay = new SecureRandom().nextInt((int) TimeUnit.MINUTES.toSeconds(250));
        int init = new SecureRandom().nextInt((int) TimeUnit.MINUTES.toSeconds(60));
        if (ConstantsFor.THIS_PC_NAME.toLowerCase().contains("no0027") ||
            ConstantsFor.THIS_PC_NAME.equalsIgnoreCase("home")) {
            init = 20;
            delay = 40;
        }
        ScheduledExecutorTask scheduledExecutorTask = new ThreadConfig().taskScheduler(runnable, init, delay);
        executor.execute(runnable);
    }

}