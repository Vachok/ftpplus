package ru.vachok.money.ctrls;



import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.config.AppComponents;
import ru.vachok.mysqlandprops.RegRuMysql;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 <h1>Request /home</h1>

 @since 22.08.2018 (10:55) */
@Controller
@Deprecated
public class Home {

   /**
    {@link RegRuMysql#getDefaultConnection(String)}
    */
   private static final Connection c = new RegRuMysql().getDefaultConnection("u0466446_liferpg");

   /**
    Время валидности {@link #setCookies(Cookie[], File, String, boolean, StringBuilder, Model)}
    */
   private static final int EXPIRY = 90;

   private MessageToUser messageToUser = new MessageCons();

    private static Logger logger = AppComponents.getLogger();


   private boolean myPC;


   public boolean isMyPC() {
      return myPC;
   }

   /**
    Index string.

    @param model   the model
    @param request the request
    @return the string
    */
   @RequestMapping (value = {"/home"}, method = RequestMethod.GET)
   public String index(Model model, HttpServletRequest request) {
      setMyPC(request.getRemoteAddr().contains("10.10.111.") || request.getRemoteAddr().contains("0:0:0:0:0"));
      String remoteAddr = request.getRemoteAddr();
      if(!myPC) throw new UnsupportedOperationException("Impossible... ");
      String lastestSpeedInDB = getLastestSpeedInDB();
      String moneyGet = "";
      model.addAttribute("getMoney", moneyGet);
      model.addAttribute("speed", lastestSpeedInDB);
      long time = request.getSession().getCreationTime();
      logger.info(new Date(time) + " was - " + remoteAddr);
      String message = null;
      try{
         float daysWOut = ( float ) TimeUnit.MILLISECONDS.toHours(request.getSession().getCreationTime() - 1515233487000L) / 24;
         message = "Привет землянин... Твоя сессия идёт " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - request.getSession().getCreationTime()) + " сек...<p>" + request.getSession().getMaxInactiveInterval() + " getMaxInactiveInterval<p>" + request.getSession().getId() + " ID сессии\n" + "запрошен URL: " + request.getRequestURL().toString() + " ; " + request.getSession().getServletContext().getServerInfo() + " servlet info; " + daysWOut + " амбрелла...; ";
      }
      catch(Exception e){
         logger.error(e.getMessage(), e);
      }
      Cookie[] requestCookies = request.getCookies();
      File dirCOOK = new File("cook");
      boolean mkdir = dirCOOK.mkdir();
      Enumeration<String> attributeNames = request.getSession().getAttributeNames();
      StringBuilder sb = new StringBuilder();
      while(attributeNames.hasMoreElements()) sb.append(attributeNames.nextElement());
      if(requestCookies!=null){
         setCookies(requestCookies, dirCOOK, remoteAddr, mkdir, sb, model);
      }
      model.addAttribute("message", message);
      logger.info("dirCOOK = " + dirCOOK.getAbsolutePath());
      String timeLeft = "Время - деньги ... ";
      LocalTime localDateTimeNow = LocalTime.now();
      LocalTime endLocalDT = LocalTime.parse("17:30");
      long totalDay = endLocalDT.toSecondOfDay() - LocalTime.parse("08:30").toSecondOfDay();
      long l = endLocalDT.toSecondOfDay() - localDateTimeNow.toSecondOfDay();
      model.addAttribute("date", new Date().toString());
      model.addAttribute("timeleft", timeLeft + "" + l + "/" + totalDay + " sec left");
      return "home";
   }

   public void setMyPC(boolean myPC) {
      this.myPC = myPC;
   }

   private String getLastestSpeedInDB() {
      StringBuilder stringBuilder = new StringBuilder();
      try(PreparedStatement p = c.prepareStatement("select * from speed ORDER BY  speed.TimeStamp DESC LIMIT 0 , 1");
          ResultSet r = p.executeQuery()){

         while(r.next()){
            stringBuilder.append(r.getDouble("speed")).append(" speed, ").append(r.getInt("road")).append(" road, ").append(r.getDouble("TimeSpend")).append(" min spend, ").append(r.getString("TimeStamp")).append(" NOW: ").append(new Date().toString());
         }
      }
      catch(SQLException e){
         logger.error(e.getMessage(), e);
      }
      return stringBuilder.toString();
   }

   private void setCookies(Cookie[] requestCookies, File dirCOOK, String remoteAddr, boolean mkdir, StringBuilder sb, Model model) {
      for(Cookie cookie : requestCookies){
         try{
            cookie.setDomain(InetAddress.getLocalHost().getHostName());
         }
         catch(UnknownHostException e){
            logger.error(MessageFormat.format("{0}\n{1}", e.getMessage(), Arrays.toString(e.getStackTrace()).replaceAll(", ", "\n")));
         }
         cookie.setMaxAge(EXPIRY);
         cookie.setPath(dirCOOK.getAbsolutePath());
         Runtime runtime = Runtime.getRuntime();
         cookie.setValue(remoteAddr + runtime.availableProcessors() + " processors\n" + runtime.freeMemory() + "/" + runtime.totalMemory() + " memory\n" + model.asMap().toString().replaceAll(", ", "\n"));
         cookie.setComment(remoteAddr + " ip\n" + sb.toString());
         if(mkdir){
            logger.info(dirCOOK.getAbsolutePath());
         }
         try(FileOutputStream outputStream = new FileOutputStream(dirCOOK.getAbsolutePath() + "\\cook" + System.currentTimeMillis() + ".txt")){
            String s = "Domain: " + cookie.getDomain() + " name: " + cookie.getName() +
                  " comment: " + cookie.getComment() + "\n" + cookie.getPath() + "\n" + cookie.getValue() + "\n" + new Date(System.currentTimeMillis());
            byte[] bytes = s.getBytes();
            outputStream.write(bytes, 0, bytes.length);
         }
         catch(IOException e){
            logger.error(MessageFormat.format("{0}\n{1}", e.getMessage(), Arrays.toString(e.getStackTrace()).replaceAll(", ", " ")));
         }
      }
   }


   @GetMapping("/net")
   public String netChk( Model model ) {
      String traceRt = "TRACE";
      try {
         InetAddress inetAddressNAT = InetAddress.getByName("srv-nat.eatmeat.ru");
         InetAddress inetAddressGIT = InetAddress.getByName("srv-git.eatmeat.ru");
         traceRt = Arrays.toString(inetAddressGIT.getAddress()) + "<p>" + Arrays.toString(inetAddressNAT.getAddress());
      } catch (UnknownHostException e) {
         logger.error(e.getMessage() , e);
      }
      List<File> bsdUpd = new FreeBSD().getPortmasterL();
      model.addAttribute("trace" , traceRt);
      model.addAttribute("files" , Arrays.toString(bsdUpd.toArray()));
      return "net";
   }

}
