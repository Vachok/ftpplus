package ru.vachok.networker.workers;


import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vachok.networker.ApplicationConfiguration;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;


/**
 <h1>Проблема Netscape Plugin</h1>
 */
@Controller
public class NPAPIResolver {

   private Logger logger = ApplicationConfiguration.logger();

   @RequestMapping ("/npapi")
   public void npapi(HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException {
      logger.getName();
      Runtime runtimeFox = Runtime.getRuntime();
      Process exec = runtimeFox.exec("\"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe\"");
      if(exec.isAlive()){
         File file = new File("file.f");
         try(ServletOutputStream outputStream1 = response.getOutputStream()){
            runtimeFox.exec("notepad.exe");
         }
         catch(IOException eR){
            logger.error(eR.getMessage());
         }
      }
      String alive = exec.toString();
      logger.info(alive);
   }
}



