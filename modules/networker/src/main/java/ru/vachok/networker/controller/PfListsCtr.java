package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PfLists;
import ru.vachok.networker.logic.DBMessenger;
import ru.vachok.networker.services.PfListsSrv;
import ru.vachok.networker.services.VisitorSrv;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 The type Index controller.
 */
@Controller
public class PfListsCtr {

    /*Fields*/
    private static final Map<String, String> SHOW_ME = new ConcurrentHashMap<>();

    private static Logger logger = AppComponents.getLogger();

    private MessageToUser messageToUser = new DBMessenger();

    private ApplicationContext appCtx = IntoApplication.getAppCtx();

    private VisitorSrv visitorSrv = appCtx.getBean(VisitorSrv.class);

    /**
     Map to show map.

     @param httpServletRequest  the http servlet request
     @param httpServletResponse the http servlet response
     @return the map
     @throws IOException the io exception
     */
    @RequestMapping ("/ind")
    public String mapToShow(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Model model) throws IOException {
        SHOW_ME.put("addr", httpServletRequest.getRemoteAddr());
        SHOW_ME.put("host", httpServletRequest.getRequestURL().toString());
        SHOW_ME.forEach((x, y) -> messageToUser.info(this.getClass().getSimpleName(), x, y));
        SHOW_ME.put("status", httpServletResponse.getStatus() + " " + httpServletResponse.getBufferSize() + " buff");
        String s = httpServletRequest.getQueryString();
        if(s!=null){
            SHOW_ME.put(this.toString(), s);
            if(s.contains("go")){
                httpServletResponse.sendRedirect("http://ftpplus.vachok.ru/docs");
            }
        }
        model.addAttribute("constfor", ConstantsFor.consString());
        return "index";
    }

    @GetMapping ("/pflists")
    public String pfBean(Model model, HttpServletRequest request, HttpServletResponse response) {
        PfLists pfLists = appCtx.getBean(PfLists.class);
        model.addAttribute("pfLists", pfLists);
        model.addAttribute("metric", Thread.activeCount() + " thr, active");
        model.addAttribute("vipnet", pfLists.getVipNet());
        model.addAttribute("tempfull", pfLists.getFullSquid());
        model.addAttribute("squidlimited", pfLists.getLimitSquid());
        model.addAttribute("squid", pfLists.getStdSquid());
        model.addAttribute("nat", pfLists.getPfNat());
        model.addAttribute("rules", pfLists.getPfRules());
        model.addAttribute("gitstats", pfLists.getGitStats());
        if (request.getQueryString() != null) PfListsSrv.buildFactory();
        String msg = response.getBufferSize() + " resp buffer";
        logger.info(msg);
        visitorSrv.makeVisit(request);
        return "pflists";
    }

    private String getAttr(HttpServletRequest request) {
        Enumeration<String> attributeNames = request.getServletContext().getAttributeNames();
        StringBuilder stringBuilder = new StringBuilder();
        while(attributeNames.hasMoreElements()){
            stringBuilder.append(attributeNames.nextElement());
            stringBuilder.append("<p>");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
