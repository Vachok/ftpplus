package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PfLists;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.services.PfListsSrv;
import ru.vachok.networker.services.VisitorSrv;

import javax.naming.TimeLimitExceededException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.rmi.UnexpectedException;
import java.rmi.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;


/**
 The type Index controller.
 */
@Controller
public class PfListsCtr {

    /*Fields*/
    private static final Map<String, String> SHOW_ME = new ConcurrentHashMap<>();

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String SOURCE_CLASS = PfListsCtr.class.getSimpleName();

    private static final String METRIC_STR = "metric";

    private VisitorSrv visitorSrv;

    private PfLists pfLists;

    private PfListsSrv pfListsSrv;

    private boolean pingOK;


    /*Instances*/
    @Autowired
    public PfListsCtr(PfLists pfLists, VisitorSrv visitorSrv) {
        AppComponents appComponents = new AppComponents();
        this.visitorSrv = visitorSrv;
        this.pfLists = pfLists;
        this.pfListsSrv = appComponents.pfListsSrv(pfLists);
        this.pingOK = ConstantsFor.isPingOK();
        new Thread(this::run).start();
    }

    @GetMapping ("/pflists")
    public String pfBean(Model model, HttpServletRequest request, HttpServletResponse response) throws UnknownHostException {
        String pflistsStr = "pflists";
        if(!pingOK){
            model.addAttribute("vipnet", "No ping to srv-git");
            model.addAttribute(METRIC_STR, LocalTime.now().toString());
            throw new UnknownHostException("srv-git");
        }
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APP_NAME + SOURCE_CLASS);
        Properties properties = initProperties.getProps();
        Long lastScan = Long.parseLong(properties.getProperty("pfscan"));
        int aThreadsLast = Integer.parseInt(properties.getProperty("thr"));

        try{
            visitorSrv.makeVisit(request);
        }
        catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        }

        model.addAttribute(METRIC_STR, ( float ) TimeUnit.MILLISECONDS
            .toSeconds(System.currentTimeMillis() - pfLists.getGitStats()) / ConstantsFor.ONE_HOUR_IN_MIN + " min since upd");
        model.addAttribute("vipnet", pfLists.getVipNet());
        model.addAttribute("tempfull", pfLists.getFullSquid());
        model.addAttribute("squidlimited", pfLists.getLimitSquid());
        model.addAttribute("squid", pfLists.getStdSquid());
        model.addAttribute("nat", pfLists.getPfNat());
        model.addAttribute("rules", pfLists.getPfRules());
        model.addAttribute("gitstats", Thread.activeCount() + " thr, active\nChange: " +
            (Thread.activeCount() - aThreadsLast));
        long timeOut = lastScan + TimeUnit.MINUTES.toMillis(15);

        initProperties.delProps();
        String msg1 = properties.toString() + " DEL! true";
        LOGGER.warn(msg1);
        properties.setProperty("pfscan", System.currentTimeMillis() + "");
        properties.setProperty("activethreads", Thread.activeCount() + "");
        initProperties.setProps(properties);
        if(request.getQueryString()!=null && System.currentTimeMillis() > timeOut){
            model.addAttribute("pfLists", pfLists);

            new Thread(() -> {
                try{
                    pfListsSrv.buildFactory();
                }
                catch(UnexpectedException | TimeLimitExceededException e){
                    LOGGER.error(e.getMessage(), e);
                }
            }).start();
        }
        if(pfLists.getTimeUpd() + TimeUnit.MINUTES.toMillis(170) < System.currentTimeMillis()){
            model.addAttribute(METRIC_STR, "Требуется обновление!");
            try{
                pfListsSrv.buildFactory();
            }
            catch(UnexpectedException | TimeLimitExceededException e){
                LOGGER.error(e.getMessage(), e);
            }
        }
        else{
            String msg = "" + ( float ) (TimeUnit.MILLISECONDS
                                             .toSeconds(System.currentTimeMillis() - pfLists.getTimeUpd())) / 60;
            LOGGER.warn(msg);
        }

        return pflistsStr;
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

    private void run() {
        try{
            pfListsSrv.buildFactory();
        }
        catch(UnexpectedException | TimeLimitExceededException e){
            LOGGER.error(e.getMessage(), e);
        }
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
