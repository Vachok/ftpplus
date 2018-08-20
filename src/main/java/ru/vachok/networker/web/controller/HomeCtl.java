package ru.vachok.networker.web.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 <h1>Запрос /home</h1>

 @since 20.08.2018 (13:41) */
@Controller
public class HomeCtl implements FaceOfControllers {

   public HomeCtl() {
      super();
   }

   @Override
   @GetMapping ("/home")
   public void proCess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ServletContext servletContext, ITemplateEngine iTemplateEngine) throws Exception {
      WebContext ctx = new WebContext(httpServletRequest, httpServletResponse, servletContext, httpServletRequest.getLocale());
      iTemplateEngine.process("home", ctx, httpServletResponse.getWriter());
   }
}
