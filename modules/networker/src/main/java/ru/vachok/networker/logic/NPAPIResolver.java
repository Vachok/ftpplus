package ru.vachok.networker.logic;


import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;


/**
 <h1>Проблема Netscape Plugin</h1>
 */
@Controller
public class NPAPIResolver {

   @GetMapping("/npapi")
   public void npapi(HttpServletRequest httpServletRequest, HttpServletResponse response) {
       throw new UnsupportedOperationException("Not Ready 05.09.2018 (22:16)"); //todo 05.09.2018 (22:16)
   }
}



