package ru.vachok.networker.web.controller;


import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import ru.vachok.networker.logic.money.BankService;
import ru.vachok.networker.logic.money.Banking;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 <h1>Контроллер запроса /product/list</h1>

 @since 20.08.2018 (12:11) */
public class BankProductsController implements FaceOfControllers {

   @Override
   public void proCess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                       ServletContext servletContext, ITemplateEngine iTemplateEngine) throws Exception {
      final BankService bankService = new BankService();
      final List<Banking> allBankings = bankService.findAll();
      final WebContext ctx = new WebContext(httpServletRequest, httpServletResponse, servletContext, httpServletRequest.getLocale());
      ctx.setVariable("prods", allBankings);
      iTemplateEngine.process("product/list", ctx, httpServletResponse.getWriter());
   }
}
