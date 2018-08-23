package ru.vachok.money;


import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


/**
 @since 20.08.2018 (17:08) */
@Controller
public class Index {

   private static final Connection u0466446Webapp = new RegRuMysql().getDefaultConnection("u0466446_webapp");

   private static Logger logger = new ApplicationConfiguration().getLogger();

   private static Marker marker = ApplicationConfiguration.marker();

   private static DataConnectTo dataConnectTo = new RegRuMysql();

   private static CookieMaker cookieMaker = new CookieMaker();

   @GetMapping ("/")
   public String indexString(HttpServletRequest request, HttpServletResponse response) {
      ConstantsFor.setMyPC(request.getRemoteAddr().contains("10.10.111.") || request.getRemoteAddr().contains("0:0:0:0:0"));
      List<Cookie> cookies = new ArrayList<>();
      request.getParameterMap().forEach((x, y) -> {
         Cookie defCookie = cookieMaker.getCookedCookie();
         defCookie.setComment(x + Arrays.toString(y).replaceAll(", ", "\n"));
         response.addCookie(defCookie);
         cookies.add(defCookie);
      });
      try{
         makeCookies(request, response);
      }
      catch(IOException e){
         logger.error(e.getMessage(), e);
      }
      return "redirect:/home";
   }

   public static void makeCookies(HttpServletRequest request, HttpServletResponse response) throws IOException {
      List<Cookie> cookies = Arrays.asList(request.getCookies());
      Function<Cookie, InputStream> getBytesStream = (x) -> {
         byte[] cook = String.format("%s name, %s domain, %d ttl,%s", x.getName(), x.getDomain(), x.getMaxAge(), x.getComment()).getBytes();
         InputStream inputStream = new ByteArrayInputStream(cook);
         return inputStream;
      };
      if(!cookies.isEmpty()){
         String sql = "insert into ru_vachok_money (classname, msgtype, bins) values  (?,?,?)";
         boolean b = writeDB(getBytesStream, cookies, sql);
         if(b) response.setStatus(200);
      }
      else{ response.sendError(418); }
   }

   private static boolean writeDB(Function<Cookie, InputStream> getBytesStream, List<Cookie> cookies, String sql) {
      try(PreparedStatement preparedStatement = u0466446Webapp.prepareStatement(sql)){
         Marker cookiesSendStarting = MarkerFactory.getMarker(Index.class.getName() + " database UPD started at " + System.currentTimeMillis());
         Index.marker.add(cookiesSendStarting);
         dataConnectTo.getSavepoint(u0466446Webapp);
         preparedStatement.setString(1, Index.class.getName());
         preparedStatement.setString(2, "web_cookies");
         preparedStatement.setBinaryStream(3, getBytesStream.apply(cookies.listIterator().next()));
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
