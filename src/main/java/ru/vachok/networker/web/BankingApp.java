package ru.vachok.networker.web;


import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import ru.vachok.networker.web.controller.BankProductsController;
import ru.vachok.networker.web.controller.FaceOfControllers;
import ru.vachok.networker.web.controller.HomeCtl;
import ru.vachok.networker.web.controller.OrederController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 <b>Controller для основной странички рассчётов</b>

 @since 20.08.2018 (11:54) */
public class BankingApp {

   private TemplateEngine templateEngine;

   private Map<String, FaceOfControllers> controllersByURL;

   public BankingApp(final ServletContext servletContext) {
      super();
      ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
      templateResolver.setSuffix("html");
      templateResolver.setCacheTTLMs(TimeUnit.MINUTES.toMillis(10));
      templateResolver.setCacheable(true);
      this.templateEngine = new TemplateEngine();
      this.templateEngine.setTemplateResolver(templateResolver);
      this.controllersByURL = new HashMap<>();
      this.controllersByURL.put("/home", new HomeCtl());
      this.controllersByURL.put("/product/list", new BankProductsController());
      this.controllersByURL.put("/orders", new OrederController());
   }

   public ITemplateEngine getEngine() {
      return this.templateEngine;
   }

   public FaceOfControllers resolveControllerForReq(final HttpServletRequest request) {
      final String path = getReqPath(request);
      return this.controllersByURL.get(path);
   }

   private String getReqPath(final HttpServletRequest request) {
      String reqURI = request.getRequestURI();
      final String contPath = request.getContextPath();
      final int fragmentIndex = reqURI.indexOf(';');
      if(fragmentIndex!=-1){
         reqURI = reqURI.substring(0, fragmentIndex);
      }
      if(reqURI.startsWith(contPath)){
         return reqURI.substring(contPath.length());
      }
      return reqURI;
   }
}
