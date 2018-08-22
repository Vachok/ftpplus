package ru.vachok.networker.web.controller;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.logic.SaverByOlder;
import ru.vachok.networker.logic.ssh.ListInternetUsers;
import ru.vachok.networker.web.ApplicationConfiguration;
import ru.vachok.networker.web.beans.ThisPC;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 The type Index controller.
 */
@Controller
public class IndexController {

   private static final Map<String, String> SHOW_ME = new ConcurrentHashMap<>();

   private static String pcName = new ThisPC().getPcName();

   private MessageToUser messageToUser = new MessageCons();

   private Logger logger = ApplicationConfiguration.logger();

   /**
    Map to show map.

    @param httpServletRequest  the http servlet request
    @param httpServletResponse the http servlet response
    @return the map
    @throws IOException the io exception
    */
   @RequestMapping ("/docs")
   @ResponseBody
   public Map<String, String> mapToShow(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
      ExecutorService executorService = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor());
      Runnable r = new SaverByOlder(SHOW_ME);
      SHOW_ME.put("addr", httpServletRequest.getRemoteAddr());
      SHOW_ME.put("host", httpServletRequest.getRequestURL().toString());
      SHOW_ME.forEach((x, y) -> messageToUser.info(this.getClass().getSimpleName(), x, y));
      SHOW_ME.put("status", httpServletResponse.getStatus() + " " + httpServletResponse.getBufferSize() + " buff");
      String s = httpServletRequest.getQueryString();
      if(s!=null){
         SHOW_ME.put(this.toString(), s);
         if(s.contains("go")) httpServletResponse.sendRedirect("http://ftpplus.vachok.ru/docs");
         executorService.execute(r);
      }
      executorService.execute(r);
      return SHOW_ME;
   }

   /**
    Addr in locale stream.

    @param httpServletRequest  the http servlet request
    @param httpServletResponse the http servlet response
    @return the stream
    @throws IOException the io exception
    */
   @RequestMapping ("/vir")
   public String addrInLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Model model) {
      List<String> namesFile = new ArrayList<>();
      String re = "redirect:https://vachok.testquality.com/project/3260/plan/6672/test/86686";
      Cookie cooki = new Cookie("hi", re);
      httpServletResponse.addCookie(cooki);
      byte[] bs = new byte[0];
      try(ServletInputStream in = httpServletRequest.getInputStream()){

         while(in.isReady()){
            in.read(bs);
         }
      }
      catch(IOException e){
         ApplicationConfiguration.logger().error(e.getMessage(), e);
      }
      messageToUser.info("HTTP Servlets Controller", httpServletRequest.getServletPath() + re, "1 КБ resp: " + new String(bs, StandardCharsets.UTF_8));
      String s = LocalDateTime.of(2018, 10, 14, 7, 0).format(DateTimeFormatter.ofPattern("dd/MM/yy"));
      Map<String, String> getEnv = System.getenv();
      getEnv.forEach((x, y) -> namesFile.add(x + "\n" + y));
      namesFile.add(re);
      namesFile.add(new String(bs, StandardCharsets.UTF_8));
      namesFile.add(s);
      namesFile.add(httpServletRequest.toString());
      namesFile.add(httpServletRequest.getSession().getServletContext().getServerInfo());
      namesFile.add(httpServletRequest.getSession().getServletContext().getServletContextName());
      namesFile.add(httpServletRequest.getSession().getServletContext().getVirtualServerName());
      namesFile.add(httpServletRequest.getSession().getServletContext().getContextPath());
      namesFile.add(Arrays.toString(httpServletResponse.getHeaderNames().toArray()));
      for(String name : namesFile){
         model.addAttribute("virTxt", name);
         return "vir";
      }
      throw new UnsupportedOperationException();
   }

   /**
    Exit app.

    @param httpServletRequest the http servlet request
    @throws IOException the io exception
    */
   @RequestMapping ("/stop")
   public void exitApp(HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException {
      String s = httpServletRequest.getRequestURL().toString();
      messageToUser.infoNoTitles(s);
      String q = httpServletRequest.getQueryString();
      if(q!=null){
         messageToUser.infoNoTitles(q);
         if(q.contains("full")) Runtime.getRuntime().exec("shutdown /p /f");
         if(q.contains("restart")) Runtime.getRuntime().exec("shutdown /r /f");
      }
      else{
         response.sendRedirect("http://10.10.111.57/e");
         System.exit(0);
      }
   }

   @RequestMapping ("/")
   public String indexModel(HttpServletRequest request, HttpServletResponse response, Model model) {
      if (request.getRemoteAddr().contains("0:0:0:0:0") || request.getRemoteAddr().contains("10.10.111."))
         return "redirect:/home";
      if (!request.getRemoteAddr().contains("10.200.213.")) return "redirect:/error";
      String usersInet = new ListInternetUsers().call();
      model.addAttribute("greetings", usersInet);
      return "index";
   }

   @GetMapping ("/f")
   public String f(HttpServletResponse httpServletResponse) {
      if(pcName.equalsIgnoreCase("home")){
         return "redirect:http://10.10.111.57:8881/ftp";
      }
      else{ throw new UnsupportedOperationException("Impossible here... " + pcName); }
   }

   private String getAttr(HttpServletRequest request) {
      Enumeration<String> attributeNames = request.getServletContext().getAttributeNames();
      StringBuilder stringBuilder = new StringBuilder();
      while(attributeNames.hasMoreElements()){
         stringBuilder.append(attributeNames.nextElement());
         stringBuilder.append("<p>");
         stringBuilder.append("\n");
      }
      return stringBuilder.toString();
   }

}
