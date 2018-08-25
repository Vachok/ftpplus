package ru.vachok.networker;



import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.web.ConstantsFor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * The type Inetor application.
 */
@EnableAutoConfiguration
@SpringBootApplication
public class InetorApplication {

    private static Logger logger = ApplicationConfiguration.logger();


    /**
     * The entry point of application.
     * <a href="https://goo.gl/K93z2L">APP Engine</a>
     *
     * @param args the input arguments
     */
    public static void main( String[] args ) {
        SpringApplication.run(InetorApplication.class , args);
        speedRun();
    }


    public static boolean dataSender( HttpServletResponse response , HttpServletRequest request , String srcClass ) {
        String sql = "insert into ru_vachok_networker (classname, msgtype, msgvalue) values (?,?,?)";
        try (Connection c = new RegRuMysql().getDefaultConnection(ConstantsFor.DB_PREFIX + "webapp");
             PreparedStatement p = c.prepareStatement(sql);) {
            p.setString(1 , srcClass);
            p.setString(2 , "request");
            p.setString(3 , request.getRemoteHost() +
                    "\n" + request.getRemoteAddr() +
                    "\n" + Arrays.toString(request.getCookies())+
                    "\n"+request.getServletContext().getServerInfo());
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            InetorApplication.logger.error(e.getMessage() , e);
            return false;
        }
    }

    private static void speedRun(){
        Runnable speedRunActualize = new SpeedRunActualize();
        ScheduledExecutorService executorService =
                Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        executorService.scheduleWithFixedDelay(speedRunActualize, 10, 300, TimeUnit.SECONDS);
    }
}