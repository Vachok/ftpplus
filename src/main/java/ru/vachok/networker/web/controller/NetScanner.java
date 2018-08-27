package ru.vachok.networker.web.controller;


import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ApplicationConfiguration;
import ru.vachok.networker.web.beans.ToStringFrom;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 @since 21.08.2018 (14:40) */
@Controller
public class NetScanner {

   private static Logger logger = ApplicationConfiguration.logger();

   @GetMapping ("/netscan")
   public String getPCNames(HttpServletRequest request, Model model) throws IOException {
      String qer = request.getQueryString();
      Collection<String> pcNames = new ArrayList<>();
      boolean reachable = false;
      InetAddress byName = null;
      for(String pcName : getCycleNames(qer)){
         try{
            byName = InetAddress.getByName(pcName);
            reachable = byName.isReachable(800);
            if(reachable){
               String onLines = ("<b> online </b> <i>" + false + "</i>");
               pcNames.add(pcName + ":" + byName.getHostAddress() + " <b>" + onLines + " </b>");
               logger.warn(pcName + " " + onLines);
            }
            else{
               String onLines = ("<b> online </b>" + true);
               pcNames.add(pcName + ":" + byName.getHostAddress() + "<b>" + onLines + "</b>");
               logger.warn(pcName + " " + onLines);
            }
         }
         catch(UnknownHostException | NullPointerException ignore){
            //
         }
      }
      String b = writeDB(pcNames);
      pcNames.add(b + " WRITE TO DB");
//      model.addAttribute("pc", "<p>" + Arrays.toString(pcNames.toArray()).replaceAll(", ", "<br>") + "</p>");
      model.addAttribute("pc", b);
      return "netscan";
   }

   private Collection<String> getCycleNames(String userQuery) {
      int inDex = getNamesCount(userQuery);
      String nameCount;
      Collection<String> list = new ArrayList<>();
      int pcNum = 0;
      for(int i = 1; i < inDex; i++){
         if(userQuery.equals("no") || userQuery.equals("pp") || userQuery.equals("do")){ nameCount = String.format("%04d", ++pcNum); }
         else{ nameCount = String.format("%03d", ++pcNum); }
         list.add(userQuery + nameCount + ".eatmeat.ru");
      }
      return list;
   }

   private String writeDB(Collection<String> pcNames) {
      DataConnectTo dataConnectTo = new RegRuMysql();
      List<String> list = new ArrayList<>();
      try(Connection c = dataConnectTo.getDefaultConnection("u0466446_velkom");
          PreparedStatement p = c.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")){
         pcNames.stream().sorted().forEach(x -> {
            String pcSerment = new String("Я не знаю...".getBytes(), StandardCharsets.UTF_8);
            logger.info(x);
            if(x.contains("200.200")) pcSerment = new String("Торговый дом".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.201")) pcSerment = new String("IP телефоны".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.202")) pcSerment = new String("Техслужба".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.203")) pcSerment = new String("СКУД".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.204")) pcSerment = new String("Упаковка".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.205")) pcSerment = new String("".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.206")) pcSerment = new String("".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.207")) pcSerment = new String("Сырокопоть".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.208")) pcSerment = new String("".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.209")) pcSerment = new String("".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.210")) pcSerment = new String("Мастера колб".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.212")) pcSerment = new String("Мастера деликатесов".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.213")) pcSerment = new String("2й этаж. АДМ.".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.214")) pcSerment = new String("WiFiCorp".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.215")) pcSerment = new String("WiFiFree".getBytes(), StandardCharsets.UTF_8);
            if(x.contains("200.217")) pcSerment = new String("1й этаж АДМ".getBytes(), StandardCharsets.UTF_8);
            boolean onLine = false;
            try{
               if(x.contains("true")) onLine = true;
               String x1 = x.split(":")[0];
               p.setString(1, x1);
               String x2 = x.split(":")[1];
               p.setString(2, x2);
               p.setString(3, pcSerment);
               p.setBoolean(4, onLine);
               p.executeUpdate();
               list.add(x1 + " " + x2 + " " + pcSerment + " " + onLine);
            }
            catch(SQLException e){
               logger.error(e.getMessage(), e);
            }
         });
         return new ToStringFrom().fromArr(list);
      }
      catch(SQLException e){
         logger.error(e.getMessage(), e);
         return e.getMessage();
      }
   }

   private int getNamesCount(String qer) {
      int inDex = 0;
      if(qer.equals("no")) inDex = 50;
      if(qer.equals("pp")) inDex = 80;
      if(qer.equals("do")) inDex = 300;
      if(qer.equals("a")) inDex = 350;
      if(qer.equals("td")) inDex = 10;
      return inDex;
   }
}
