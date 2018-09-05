package ru.vachok.networker;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.beans.DBMessenger;
import ru.vachok.networker.config.AppComponents;
import ru.vachok.networker.logic.StringFromArr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
public class IntoApplication {

    private static HttpServletRequest req;

    private static final MessageToUser DB_MSG = new DBMessenger();

    private static final String SOURCE_CLASS = IntoApplication.class.getSimpleName();

    /*Get&*/
    public static void setReq(HttpServletRequest req) {
        IntoApplication.req = req;
    }

    /*PS Methods*/

    /**
     The entry point of application.
     <a href="https://goo.gl/K93z2L">APP Engine</a>

     @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(IntoApplication.class, args);
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(AppComponents.class);
        infoForU();
    }

    private static void infoForU() {
        float hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - ConstantsFor.START_STAMP);
        DB_MSG.info(IntoApplication.class.getSimpleName(), "INFO", +hours + " h\n" + "Started at " +
                                                                   new Date(ConstantsFor.START_STAMP).toString() + "\n" +
                                                                   getInetAddr());
    }

    private static String getInetAddr() {
        try{
            return InetAddress.getLocalHost().getHostAddress() + " " + InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException e){
            DB_MSG.errorAlert(SOURCE_CLASS, "Inet Address", new StringFromArr().fromArr(e.getStackTrace()));
        }
        throw new UnsupportedOperationException();
    }

    public static boolean dataSender(HttpServletResponse response, HttpServletRequest request, String srcClass) {
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
        } catch(SQLException e){
            DB_MSG.errorAlert(IntoApplication.class.getSimpleName(), e.getMessage(), new StringFromArr().fromArr(e.getStackTrace()));
            return false;
        }
    }

    //unstat
}