package ru.vachok.networker.web.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.networker.web.ApplicationConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


/**
 @since 21.08.2018 (14:40) */
@Controller
public class NetScanner {

   @GetMapping ("/netscan")
   @ResponseBody
   public Stream<String> ip() {
      try{
         return getPCNames().sorted();
      }
      catch(IOException e){
         ApplicationConfiguration.logger().error(e.getMessage(), e);

      }
   }

   private Stream<String> getPCNames() throws IOException {
      List<String> pcNames = new ArrayList<>();
      String nameCount = "000";
      InetAddress inetAddress = null;
      try{
         inetAddress = InetAddress.getByName("do0" + nameCount);
      }
      catch(UnknownHostException e){
         ApplicationConfiguration.logger().error(e.getMessage(), e);
      }
      boolean reachable = inetAddress.isReachable(500);
      pcNames.add(inetAddress.toString() + " online " + reachable);

      return pcNames.parallelStream();
   }
}
