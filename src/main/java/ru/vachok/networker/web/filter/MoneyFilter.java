package ru.vachok.networker.web.filter;


import org.thymeleaf.ITemplateEngine;
import ru.vachok.networker.web.BankingApp;
import ru.vachok.networker.web.User;
import ru.vachok.networker.web.controller.FaceOfControllers;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class MoneyFilter implements Filter {


   private ServletContext servletContext;

   private BankingApp application;


   public MoneyFilter() {
      super();
   }

   public void init(final FilterConfig filterConfig) {
      this.servletContext = filterConfig.getServletContext();
      this.application = new BankingApp(this.servletContext);
   }

   public void doFilter(final ServletRequest request, final ServletResponse response,
                        final FilterChain chain) throws IOException, ServletException {
      addUserToSession(( HttpServletRequest ) request);
      if(!process(( HttpServletRequest ) request, ( HttpServletResponse ) response)){
         chain.doFilter(request, response);
      }
   }

   private static void addUserToSession(final HttpServletRequest request) {
      // Simulate a real user session by adding a user object
      request.getSession(true).setAttribute("user", new User("John", "Apricot", "Antarctica", null));
   }

   private boolean process(HttpServletRequest request, HttpServletResponse response)
         throws ServletException {

      try{

         // This prevents triggering engine executions for resource URLs
         if(request.getRequestURI().startsWith("/css") ||
               request.getRequestURI().startsWith("/images") ||
               request.getRequestURI().startsWith("/favicon")){
            return false;
         }


         /*
          * Query controller/URL mapping and obtain the controller
          * that will process the request. If no controller is available,
          * return false and let other filters/servlets process the request.
          */
         FaceOfControllers controller = this.application.resolveControllerForReq(request);
         if(controller==null){
            return false;
         }

         /*
          * Obtain the TemplateEngine instance.
          */
         ITemplateEngine templateEngine = this.application.getEngine();

         /*
          * Write the response headers
          */
         response.setContentType("text/html;charset=UTF-8");
         response.setHeader("Pragma", "no-cache");
         response.setHeader("Cache-Control", "no-cache");
         response.setDateHeader("Expires", 0);

         /*
          * Execute the controller and process view template,
          * writing the results to the response writer.
          */
         controller.proCess(
               request, response, this.servletContext, templateEngine);

         return true;

      }
      catch(Exception e){
         try{
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
         catch(final IOException ignored){
            // Just ignore this
         }
         throw new ServletException(e);
      }

   }

   public void destroy() {
      // nothing to do
   }

}