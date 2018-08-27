package ru.vachok.networker.web.controller;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.networker.ApplicationConfiguration;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;


/**
 @since 22.08.2018 (10:17) */
@Controller
public class ExecExtApps {

   private static final Logger LOGGER = ApplicationConfiguration.logger();

   @RequestMapping ("/idea")
   @ResponseBody
   public String lIdea(HttpServletRequest request) {
      String q = request.getQueryString();
      boolean alive = false;
      Process exec;
      try{
         exec = Runtime.getRuntime().exec("G:\\My_Proj\\.IdeaIC2017.3\\apps\\IDEA-C\\ch-0\\182.3684.101\\bin\\idea64.exe");
         alive = exec.isAlive();
      }
      catch(IOException e){
         LOGGER.error(e.getMessage(), e);
      }

      if(q!=null){
         if(q.contains("exe:")){
            String[] exes = q.split("exe:");
            LOGGER.info(String.format("exes = %s", Arrays.toString(exes)));
            LOGGER.info(String.format("exes[1] = %s", exes[1]));
            try{
               exec = Runtime.getRuntime().exec(exes[1]);
            }
            catch(IOException e){
               LOGGER.error(e.getMessage(), e);
            }
         }
      }
      return "IDEA is " + alive;
   }

}
