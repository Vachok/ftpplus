package ru.vachok.money;


import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.logic.TForms;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import javax.mail.*;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.Chronology;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


/**
 @since 20.08.2018 (17:08) */
@Controller
public class Index {

    /*Fields*/
    private static final String SOURCE_CLASS = Index.class.getSimpleName();

    private static final Connection u0466446Webapp = new RegRuMysql().getDefaultConnection("u0466446_webapp");

    private static Logger logger = ApplicationConfiguration.getLogger();

    private static Marker marker = ApplicationConfiguration.marker();

    private static DataConnectTo dataConnectTo = new RegRuMysql();

    private static CookieMaker cookieMaker = new CookieMaker();

    @GetMapping ("/")
    public String indexString(HttpServletRequest request, HttpServletResponse response, Model model) {
        Cookie[] cookies = request.getCookies();
        String mailString = getMailBox(request);
        if(cookies!=null){
            for(Cookie cookie : cookies){
                int maxAge = cookie.getMaxAge();
                long tStampNow = System.currentTimeMillis();
                String cookiePath = cookie.getPath();
                File cookieFile;
                try{
                    cookieFile = new File(cookiePath);
                }
                catch(Exception e){
                    continue;
                }
                long cookieStamp = cookieFile.lastModified();
                boolean delete = false;
                if(( int ) (tStampNow - cookieStamp) > maxAge){
                    delete = cookieFile.delete();
                    if(!delete){
                        cookieFile.deleteOnExit();
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                }
                String cookieInfo = cookie.getName() + " name; " + cookie.getValue() + " value; " + maxAge + " cookie delete is " + delete;
                logger.info(cookieInfo);
            }
            return "redirect:/ftp";
        }
        model.addAttribute("mail", mailString);
        ConstantsFor.ok
            .accept(SOURCE_CLASS, "returned home\n " +
                request.getRemoteHost() +
                ":" + request.getRemotePort() +
                "\n" + request.getQueryString());
        return "home";
    }

    private String getMailBox(HttpServletRequest request) {
        MailMessages mailMessages = new MailMessages();
        StringBuilder stringBuilder = new StringBuilder();
        try{
            Folder folder = mailMessages.getInbox();
            Message[] messages = folder.getMessages();
            for(Message m : messages){
                stringBuilder.append(new TForms().toStringFromArray(m.getFrom()));
                stringBuilder.append(" ").append(m.getReceivedDate()).append("<br>");
                stringBuilder.append(m.getSubject()).append("<br>");
                if(request.getQueryString()!=null){
                    if(request.getQueryString().contains("clean")){
                        m.setFlag(Flags.Flag.DELETED, true);
                    }
                }
            }
            folder.close(true);
        }
        catch(MessagingException e){
            logger.error(SOURCE_CLASS + ".getMailbox\n" + e.getMessage(), e);
            return e.getMessage() + "<br>" + TForms.toStringFromArray(e.getStackTrace());
        }

        return stringBuilder.toString();
    }

    /*Private metsods*/
    private static InputStream makeCookies(HttpServletRequest request, HttpServletResponse response) throws HttpMediaTypeNotSupportedException {
        String remoteHost = request.getRemoteHost();
        String id = request.getSession().getId();
        Cookie sessionHost = new Cookie(id, remoteHost);
        Chronology chronology = LocalDate.of(ConstantsFor.YEAR_BIRTH, ConstantsFor.MONTH_BIRTH, ConstantsFor.DAY_OF_B_MONTH).atTime(2, 0).getChronology();
        int i = chronology.compareTo(LocalDateTime.now().getChronology());
        Cookie chronoCookie = new Cookie("chrono", i + "");

        response.addCookie(sessionHost);
        response.addCookie(chronoCookie);
        Function<Cookie, InputStream> getBytesStream = (x) -> {
            InputStream inputStream = null;
            try{
                request.authenticate(response);
                inputStream = request.getInputStream();
            }
            catch(IOException | ServletException e){
                logger.error(e.getMessage(), e);
            }
            return inputStream;
        };
        List<Cookie> cookies = Arrays.asList(request.getCookies());
        String sql = "insert into ru_vachok_money (classname, msgtype, msgvalue) values  (?,?,?)";
        boolean b = writeDB(getBytesStream, cookies, sql);
        if(b){
            response.setStatus(HttpServletResponse.SC_OK);
        }
        else{
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
        throw new HttpMediaTypeNotSupportedException(HttpServletResponse.SC_NO_CONTENT + " no content");
    }

    private static boolean writeDB(Function<Cookie, InputStream> getBytesStream, List<Cookie> cookies, String sql) {
        try(PreparedStatement preparedStatement = u0466446Webapp.prepareStatement(sql)){
            Marker cookiesSendStarting = MarkerFactory.getMarker(Index.class.getName() + " database UPD started at " + System.currentTimeMillis());
            Index.marker.add(cookiesSendStarting);
            dataConnectTo.getSavepoint(u0466446Webapp);
            preparedStatement.setString(1, Index.class.getName());
            preparedStatement.setString(2, "web_cookies");
            preparedStatement.setString(3, cookies.toString());
            preparedStatement.executeUpdate();
            marker.remove(cookiesSendStarting);
        }
        catch(SQLException e){
            dataConnectTo.getSavepoint(u0466446Webapp);
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
}
