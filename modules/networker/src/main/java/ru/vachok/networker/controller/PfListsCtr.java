package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PfLists;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.logic.DBMessenger;
import ru.vachok.networker.services.PfListsSrv;
import ru.vachok.networker.services.VisitorSrv;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.*;


/**
 The type Index controller.
 */
@Controller
public class PfListsCtr {

    /*Fields*/
    private static final Map<String, String> SHOW_ME = new ConcurrentHashMap<>();

    private static final Logger LOGGER = AppComponents.getLogger();

    private MessageToUser messageToUser = new DBMessenger();

    private static final ApplicationContext APP_CTX = IntoApplication.getAppCtx();

    private VisitorSrv visitorSrv = APP_CTX.getBean(VisitorSrv.class);

    private PfLists pfLists = APP_CTX.getBean(PfLists.class);

    private PfListsSrv pfListsSrv = APP_CTX.getBean(PfListsSrv.class);

    @GetMapping ("/pflists")
    public String pfBean(Model model, HttpServletRequest request, HttpServletResponse response) {
        visitorSrv.makeVisit(request);
        model.addAttribute("pfLists", pfLists);
        model.addAttribute("metric", pfLists.getGitStats());
        model.addAttribute("vipnet", pfLists.getVipNet());
        model.addAttribute("tempfull", pfLists.getFullSquid());
        model.addAttribute("squidlimited", pfLists.getLimitSquid());
        model.addAttribute("squid", pfLists.getStdSquid());
        model.addAttribute("nat", pfLists.getPfNat());
        model.addAttribute("rules", pfLists.getPfRules());
        model.addAttribute("gitstats", Thread.activeCount() + " thr, active");
        if(request.getQueryString()!=null){
            new Thread(() -> pfListsSrv.buildFactory()).start();
        }
        if (pfLists.getTimeUpd() + TimeUnit.MINUTES.toMillis(170) < System.currentTimeMillis()) {
            model.addAttribute("metric", "Требуется обновление!");
            pfListsSrv.buildFactory();
            return "pflists";
        }
        else{
            String msg = "" + ( float ) (TimeUnit
                                             .MILLISECONDS.toSeconds(System.currentTimeMillis() - pfLists.getTimeUpd())) / 60;
            LOGGER.warn(msg);
            return "pflists";
        }

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

    private void scheduleAns() {

        Runnable runnable = () -> {
            Thread.currentThread().setName("id " + System.currentTimeMillis());
            float upTime = ( float ) (System.currentTimeMillis() - ConstantsFor.START_STAMP) / TimeUnit.DAYS.toMillis(1);
            String msg = upTime +
                " uptime days. Active threads = " +
                Thread.activeCount() + ". This thread = " +
                Thread.currentThread().getName() + "|" +
                System.currentTimeMillis() + "\n";
            LOGGER.warn(msg);
            Thread.currentThread().interrupt();
        };
        int delay = new SecureRandom().nextInt(( int ) TimeUnit.MINUTES.toMillis(250));
        int init = new SecureRandom().nextInt(( int ) TimeUnit.MINUTES.toMillis(60));
        if(ConstantsFor.THIS_PC_NAME.toLowerCase().contains("no0027") ||
            ConstantsFor.THIS_PC_NAME.equalsIgnoreCase("home")){
            init = 20000;
            delay = 40000;
        }
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadConfig().threadPoolTaskScheduler();
        ScheduledFuture<?> schedule = threadPoolTaskScheduler.scheduleWithFixedDelay(runnable, new Date(), delay);
        try{
            schedule.get(35, TimeUnit.SECONDS);
        }
        catch(InterruptedException | ExecutionException | TimeoutException e){
            long delay1 = schedule.getDelay(TimeUnit.SECONDS);
            LOGGER.error(e.getMessage() + " " + delay1 + " delay", e);
            Thread.currentThread().interrupt();
        }
    }

}
