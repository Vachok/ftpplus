package ru.vachok.networker.accesscontrol;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
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
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 {@link Controller} для {@code /pflists}
 <p>
 <a href="/pflists" target=_blank>Pf Lists</a>

 @since 14.11.2018 (15:11) */
@Controller
public class PfListsCtr {

    /**
     {@link LoggerFactory#getLogger(String)}
     */
    private static final ThreadLocal<Logger> LOGGER = ThreadLocal.withInitial(() -> LoggerFactory.getLogger(PfListsCtr.class.getSimpleName()));

    /**
     {@link ThreadLocal} {@link String} - {@code metric}
     */
    private static final ThreadLocal<String> METRIC_STR = ThreadLocal.withInitial(() -> "metric");

    /**
     {@link ThreadConfig#threadPoolTaskExecutor()}
     */
    private static final TaskExecutor TASK_EXECUTOR = AppComponents.threadConfig().threadPoolTaskExecutor();

    /**
     {@link AppComponents#getProps()}
     */
    private final ThreadLocal<Properties> properties = ThreadLocal.withInitial(AppComponents::getProps);

    /**
     {@link PfLists}
     */
    private PfLists pfLists;

    /**
     {@link PfListsSrv}
     */
    private PfListsSrv pfListsSrv;

    /**
     {@link ConstantsFor#isPingOK()}
     */
    private boolean pingOK;

    /**
     {@code lastScan плюс TimeUnit.MINUTES.toMillis(15)}
     */
    private long timeOut;

    /**
     {@link #runningList()}
     */
    private final Runnable makePFLists = this::runningList;

    /**
     {@link Random#nextInt(int)} - {@link TimeUnit#toMillis(long)} <b>250</b>
     */
    private int delayRef = new SecureRandom().nextInt((int) TimeUnit.MINUTES.toMillis(250));

    /**
     Public-консттруктор.
     <p>

     @param pfLists    {@link #pfLists}
     @param pfListsSrv {@link #pfListsSrv}
     */
    @Autowired
    public PfListsCtr(PfLists pfLists, PfListsSrv pfListsSrv) {
        TASK_EXECUTOR.execute(makePFLists);
        this.pfLists = pfLists;
        this.pfListsSrv = pfListsSrv;
        this.pingOK = ConstantsFor.isPingOK();
        Thread.currentThread().setName("PfListsCtr_" + System.currentTimeMillis());
    }

    /**
     Контроллер <a href="/pflists" target=_blank>/pflists</a>
     <p>
     Запись {@link Visitor} ({@link ConstantsFor#getVis(HttpServletRequest)}). <br>
     Определение времени последнего запуска. {@link Properties#getProperty(java.lang.String, java.lang.String)} from {@link #properties} as {@link ConstantsFor#PR_PFSCAN} <br>
     this.{@link #timeOut} = последнее сканирование плюс {@link TimeUnit#toMillis(long)} <b>15</b>
     <p>
     Если {@link #pingOK}: <br>
     {@link #modSet(Model)} ; <br>
     Если {@link HttpServletRequest#getQueryString()} не {@code null}: <br>
     {@link TaskExecutor#execute(java.lang.Runnable)} - {@link #makePFLists} ; <br>
     Если {@link PfLists#getTimeUpd()} плюс 1 час к {@link ConstantsFor#DELAY} меньше чем сейчас: <br>
     {@link Model} аттрибуты: ({@link PfListsCtr#METRIC_STR} , {@code Требуется обновление!} ; ({@link ConstantsFor#ATT_GITSTATS} , )

     @param model {@link Model}
     @param request {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @return {@link ConstantsFor#PFLISTS}.html
     @throws UnknownHostException
     */
    @GetMapping("/pflists")
    public String pfBean(Model model, HttpServletRequest request, HttpServletResponse response) throws UnknownHostException {
        Visitor visitor = ConstantsFor.getVis(request);
        long lastScan = Long.parseLong(properties.get().getProperty(ConstantsFor.PR_PFSCAN, "1"));
        timeOut = lastScan + TimeUnit.MINUTES.toMillis(15);

        if (!pingOK) noPing(model);
        modSet(model);
        if (request.getQueryString() != null) {
            TASK_EXECUTOR.execute(makePFLists);
        }
        if (pfLists.getTimeUpd() + TimeUnit.MINUTES.toMillis((int) (ConstantsFor.DELAY + ConstantsFor.ONE_HOUR_IN_MIN)) < System.currentTimeMillis()) {
            model.addAttribute(METRIC_STR.get(), "Требуется обновление!");
            model.addAttribute(ConstantsFor.ATT_GITSTATS, pfListsSrv.getExecutor());
        } else {
            String msg = "" + (float) (TimeUnit.MILLISECONDS
                .toSeconds(System.currentTimeMillis() - pfLists.getTimeUpd())) / ConstantsFor.ONE_HOUR_IN_MIN;
            LOGGER.get().warn(msg);
        }
        propUpd(properties.get());
        String refreshRate = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(delayRef) * ConstantsFor.ONE_HOUR_IN_MIN);
        response.addHeader(ConstantsFor.HEAD_REFRESH, refreshRate);
        String msg = TimeUnit.MILLISECONDS.toMinutes(delayRef) + " autorefresh\n" + visitor.toString();
        LOGGER.get().info(msg);
        return ConstantsFor.PFLISTS;
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
    private static void propUpd(Properties properties) {
        properties.setProperty(ConstantsFor.PR_PFSCAN, System.currentTimeMillis() + "");
        properties.setProperty("thr", Thread.activeCount() + ". Memory info: " + ConstantsFor.getMemoryInfo());
    }

    private void modSet(Model model) {
        String metricValue = (float) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - pfLists.getGitStats()) / ConstantsFor.ONE_HOUR_IN_MIN + " min since upd";
        String gitstatValue = Thread.activeCount() + " thr, active\nChange: " + (Thread.activeCount() - Long.parseLong(properties.get().getOrDefault("thr", 1L).toString()));
        gitstatValue = gitstatValue + "\n" + ConstantsFor.getMemoryInfo() + "\n\n" + new TForms().fromArray(properties.get(), false);

        model.addAttribute("PfListsSrv", pfListsSrv);
        model.addAttribute(METRIC_STR.get(), metricValue);
        model.addAttribute("vipnet", pfLists.getVipNet());
        model.addAttribute("tempfull", pfLists.getFullSquid());
        model.addAttribute("squidlimited", pfLists.getLimitSquid());
        model.addAttribute("squid", pfLists.getStdSquid());
        model.addAttribute("nat", pfLists.getPfNat());
        model.addAttribute("rules", pfLists.getPfRules());

        model.addAttribute(ConstantsFor.ATT_GITSTATS, gitstatValue);
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());

    }

    private void noPing(Model model) throws UnknownHostException {
        model.addAttribute("vipnet", "No ping to srv-git");
        model.addAttribute(METRIC_STR.get(), LocalTime.now().toString());
        throw new UnknownHostException("srv-git");
    }

    private void runningList() {
        try {
            if (timeOut < System.currentTimeMillis()) {
                pfListsSrv.getExecutor();
            } else throw new TimeLimitExceededException(TimeUnit
                .MILLISECONDS
                .toSeconds(timeOut - System.currentTimeMillis()) / ConstantsFor.ONE_HOUR_IN_MIN + "");
        } catch (TimeLimitExceededException e) {
            LOGGER.get().error(e.getMessage(), e);
        }
    }

}
