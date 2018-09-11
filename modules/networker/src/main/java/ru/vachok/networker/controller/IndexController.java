package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.DBMessenger;
import ru.vachok.networker.beans.AppComponents;
import ru.vachok.networker.beans.PfLists;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.services.PfListsSrv;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;


/**
 The type Index controller.
 */
@Controller
public class IndexController {

    /*Fields*/
    private static final Map<String, String> SHOW_ME = new ConcurrentHashMap<>();

    private static final String SOURCE_CLASS = IndexController.class.getName();

    private static Logger logger = AppComponents.getLogger();

    private MessageToUser messageToUser = new DBMessenger();

    private ApplicationContext appCtx = AppCtx.scanForBeansAndRefreshContext();

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
        scheduleAns();
        PfLists pfLists = appCtx.getBean(PfLists.class);
        model.addAttribute("pfLists", pfLists);
        model.addAttribute("metric", PfListsSrv
            .getEndDate() + " renew| " + pfLists.getUname().split("FreeBSD")[1]);
        model.addAttribute("vipnet", pfLists.getVipNet());
        model.addAttribute("tempfull", pfLists.getFullSquid());
        model.addAttribute("squidlimited", pfLists.getLimitSquid());
        model.addAttribute("squid", pfLists.getStdSquid());
        model.addAttribute("nat", pfLists.getPfNat());
        model.addAttribute("rules", pfLists.getPfRules());
        return "pflists";
    }

    private void scheduleAns() {
        ScheduledExecutorService executorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        Runnable runnable = () -> {
            MessageToUser m = new DBMessenger();
            float upTime = ( float ) (System.currentTimeMillis() - ConstantsFor.START_STAMP) /
                TimeUnit.DAYS.toMillis(1);
            m.info(SOURCE_CLASS, "UPTIME", upTime + " days");
            new PfListsSrv().buildFactory();
        };
        int delay = new Random().nextInt(( int ) TimeUnit.MINUTES.toSeconds(17) / 3);
        int init = new Random().nextInt(( int ) TimeUnit.MINUTES.toSeconds(20));
        executorService.scheduleWithFixedDelay(runnable, init, delay, TimeUnit.MINUTES);
        String msg = runnable + " " + init + " init ," + delay + " delay";
        logger.info(msg);
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
