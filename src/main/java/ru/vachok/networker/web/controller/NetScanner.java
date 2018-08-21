package ru.vachok.networker.web.controller;


import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.catalina.servlet4preview.http.ServletMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.networker.web.ApplicationConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


/**
 @since 21.08.2018 (14:40) */
@Controller
public class NetScanner {

   @GetMapping ("/netscan")
   @ResponseBody
   public List<String> ip(HttpServletRequest request) {
      ServletMapping servletMapping = request.getServletMapping();
      List<String> ip = new ArrayList<>();
      try{
         ip = getPCNames();
         ip.add(servletMapping.getServletName());
      }
      catch(IOException e){
         ApplicationConfiguration.logger().error(e.getMessage(), e);

      }
      return ip;
   }

   private List<String> getPCNames() throws IOException {
      List<String> pcNames = new ArrayList<>();
      String nameCount = "045";
      InetAddress inetAddress;
      try{
         for(int i = 0; i < 200; i++){
            nameCount = String.format("%04d", i);
            inetAddress = InetAddress.getByName("do" + nameCount + ".eatmeat.ru");
            boolean reachable = inetAddress.isReachable(500);
            String e = inetAddress.toString() + " online " + reachable;
            pcNames.add(e);
            ApplicationConfiguration.logger().info(e);
         }
      }
      catch(UnknownHostException ignore){
         return pcNames;
      }
      return pcNames;
   }
}
