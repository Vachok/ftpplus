package ru.vachok.networker.web.controller;



import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.web.ApplicationConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 @since 21.08.2018 (14:40) */
@Controller
public class NetScanner {


   @GetMapping ("/netscan")
   @ResponseBody
   public List<String> getPCNames( HttpServletRequest request ) throws IOException {

      List<String> pcNames = new ArrayList<>();
      String nameCount;
      List<String> aList = nextA();
      InetAddress inetAddress;
      try{
         for (int i = 1; i < 200; i++) {
            nameCount = String.format("%04d", i);
            inetAddress = InetAddress.getByName("do" + nameCount + ".eatmeat.ru");
            boolean reachable = inetAddress.isReachable(100);
            String e = inetAddress.toString() + " online " + reachable;
            pcNames.add(e);
            ApplicationConfiguration.logger().warn(e);
         }
      } catch (UnknownHostException re) {
         pcNames.add(re.getMessage());
         writeDB(pcNames);
         ApplicationConfiguration.logger().error(re.getMessage() , re);
      }
      boolean b = writeDB(pcNames);
      pcNames.add(b + " WRITE TO DB");
      boolean addAll = pcNames.addAll(aList);
      ApplicationConfiguration.logger().warn(addAll + " add all");
      return pcNames;
   }


   @ResponseBody
   private List<String> nextA() {
      String nameCount;
      List<String> pcNames = new ArrayList<>();
      InetAddress inetAddress;
      try {
         for (int i = 100; i < 350; i++) {
            nameCount = String.format("%03d" , i);
            inetAddress = InetAddress.getByName("a" + nameCount + ".eatmeat.ru");
            boolean reachable = inetAddress.isReachable(100);
            String e = inetAddress.toString() + " online " + reachable;
            pcNames.add(e);
            ApplicationConfiguration.logger().warn(e);
         }
      } catch (IOException re) {
         pcNames.add(re.getMessage());
         writeDB(pcNames);

         ApplicationConfiguration.logger().error(re.getMessage() , re);
      }
      return pcNames;
   }


   private boolean writeDB( List<String> pcNames ) {
       DataConnectTo dataConnectTo = new RegRuMysql();

       try (Connection c = dataConnectTo.getDataSource().getConnection(); PreparedStatement p = c.prepareStatement("insert into  u0466446_liferpg.velkompc (instr) values (?)")) {
         pcNames.stream().sorted().forEach(x -> {
            try {
               p.setString(1 , x);
               p.executeUpdate();
            } catch (SQLException e) {
               ApplicationConfiguration.logger().error(e.getMessage() , e);
            }
         });
          return true;
      } catch (SQLException e) {
         ApplicationConfiguration.logger().error(e.getMessage() , e);
          return false;
      }
   }
}
