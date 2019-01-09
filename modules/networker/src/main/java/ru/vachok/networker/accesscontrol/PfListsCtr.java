package ru.vachok.networker.accesscontrol;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;

import javax.naming.TimeLimitExceededException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.rmi.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 The type Index controller.
 */
@Controller
public class PfListsCtr {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String METRIC_STR = "metric";

    private static String thisPcName;

    private Properties properties;

    private Visitor visitor;

    private PfLists pfLists;

    private PfListsSrv pfListsSrv;

    private boolean pingOK;

    private long timeOut;

    private int delayRef = new SecureRandom().nextInt((int) TimeUnit.MINUTES.toMillis(250));

    private final Runnable makePFLists = this::runningList;

    private ThreadConfig threadConfig = new ThreadConfig();

    /*Instances*/
    @Autowired
    public PfListsCtr(PfLists pfLists, PfListsSrv pfListsSrv) {
        threadConfig.taskDecorator(makePFLists);
        this.properties = ConstantsFor.getProps();
        this.pfLists = pfLists;
        this.pfListsSrv = pfListsSrv;
        this.pingOK = ConstantsFor.isPingOK();
    }

    static {
        thisPcName = ConstantsFor.thisPC();
    }

    @GetMapping("/pflists")
    public String pfBean(Model model, HttpServletRequest request, HttpServletResponse response) throws UnknownHostException {
        this.visitor = new Visitor(request);
        String pflistsStr = "pflists";
        this.properties = ConstantsFor.getProps();
        long lastScan = Long.parseLong(properties.getProperty("pfscan", "1"));
        timeOut = lastScan + TimeUnit.MINUTES.toMillis(15);
        if (!pingOK) noPing(model);
        try {
            LOGGER.warn(visitor.toString());
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
        modSet(model);
        if (request.getQueryString() != null) threadConfig.taskDecorator(makePFLists);
        if (pfLists.getTimeUpd() + TimeUnit.MINUTES.toMillis(170) < System.currentTimeMillis()) {
            model.addAttribute(METRIC_STR, "Требуется обновление!");
            model.addAttribute("gitstats", pfListsSrv.getExecutor().toString());
        } else {
            String msg = "" + (float) (TimeUnit.MILLISECONDS
                .toSeconds(System.currentTimeMillis() - pfLists.getTimeUpd())) / ConstantsFor.ONE_HOUR_IN_MIN;
            LOGGER.warn(msg);
        }
        propUpd(properties);
        String refreshRate = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(delayRef) * ConstantsFor.ONE_HOUR_IN_MIN);
        response.addHeader(ConstantsFor.HEAD_REFRESH, refreshRate);
        String msg = TimeUnit.MILLISECONDS.toMinutes(delayRef) + " autorefresh";
        LOGGER.info(msg);
        return pflistsStr;
    }

    private void modSet(Model model) {
        model.addAttribute("PfListsSrv", pfListsSrv);
        model.addAttribute(METRIC_STR, (float) TimeUnit.MILLISECONDS
            .toSeconds(System.currentTimeMillis() - pfLists.getGitStats()) / ConstantsFor.ONE_HOUR_IN_MIN + " min since upd");
        model.addAttribute("vipnet", pfLists.getVipNet());
        model.addAttribute("tempfull", pfLists.getFullSquid());
        model.addAttribute("squidlimited", pfLists.getLimitSquid());
        model.addAttribute("squid", pfLists.getStdSquid());
        model.addAttribute("nat", pfLists.getPfNat());
        model.addAttribute("rules", pfLists.getPfRules());
        model.addAttribute("gitstats", Thread.activeCount() + " thr, active\nChange: " +
            (Thread.activeCount() - Long.parseLong(properties.getOrDefault("thr", 1L).toString())));
        model.addAttribute("footer", new PageFooter().getFooterUtext());

    }

    private void noPing(Model model) throws UnknownHostException {
        model.addAttribute("vipnet", "No ping to srv-git");
        model.addAttribute(METRIC_STR, LocalTime.now().toString());
        throw new UnknownHostException("srv-git");
    }

    @PostMapping("/runcom")
    public String runCommand(Model model, @ModelAttribute PfListsSrv pfListsSrv) {
        this.pfListsSrv = pfListsSrv;
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute(ConstantsFor.ATT_TITLE, pfListsSrv.getCommandForNat());
        model.addAttribute("PfListsSrv", pfListsSrv);
        model.addAttribute("ok", pfListsSrv.runCom());
        return "ok";
    }

    /**
     @param properties {@link ConstantsFor#PROPS}
     */
    private void propUpd(Properties properties) {

        properties.setProperty("pfscan", System.currentTimeMillis() + "");
        properties.setProperty("thr", Thread.activeCount() + "");
    }

    private void runningList() {
        try {
            if (timeOut < System.currentTimeMillis()) {
                pfListsSrv.getExecutor();
            } else throw new TimeLimitExceededException(TimeUnit
                .MILLISECONDS
                .toSeconds(timeOut - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN + "");
        } catch (TimeLimitExceededException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
