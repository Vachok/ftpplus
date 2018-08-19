package ru.vachok.networker.workers;



import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ApplicationConfiguration;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 <h1>Проблема Netscape Plugin</h1>
 */
@Controller
public class NPAPIResolver {

   private Logger logger = ApplicationConfiguration.logger();


   @GetMapping("/npapi")
   public void npapi( HttpServletRequest httpServletRequest , HttpServletResponse response ) throws IOException {
      String loggerName = logger.getName();

   }
}



