package ru.vachok.networker.accesscontrol;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.services.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.rmi.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(PfListsCtr.class.getSimpleName());

    /**
     {@link ThreadLocal} {@link String} - {@code metric}
     */
    private static final @NotNull String ATT_METRIC = "metric";

    private static final int DELAY_LOCAL_INT = (int) (ConstantsFor.DELAY + ConstantsFor.ONE_HOUR_IN_MIN);

    private static final String ATT_VIPNET = "vipnet";

    /**
     {@link AppComponents#getOrSetProps()}
     */
    private final Properties properties = AppComponents.getOrSetProps();

    /**
     {@link PfLists}
     */
    private PfLists pfListsInstAW;

    /**
     {@link ConstantsFor#isPingOK()}
     */
    private boolean pingGITOk;

    /**
     {@link Random#nextInt(int)} - {@link TimeUnit#toMillis(long)} <b>250</b>
     */
    private final int delayRefInt = new SecureRandom().nextInt((int) TimeUnit.MINUTES.toMillis(250));

    /**
     {@link PfListsSrv}
     */
    private PfListsSrv pfListsSrvInstAW;

    /**
     {@code lastScan плюс TimeUnit.MINUTES.toMillis(15)}
     */
    private long timeOutLong = 1L;

    /**
     {@link MessageLocal}
     */
    private MessageToUser messageToUser = new MessageLocal();

    /**
     Public-консттруктор.
     <p>

     @param pfLists    {@link #pfListsInstAW}
     @param pfListsSrv {@link #pfListsSrvInstAW}
     */
    @Autowired
    public PfListsCtr(PfLists pfLists, PfListsSrv pfListsSrv) {
        this.pfListsInstAW = pfLists;
        this.pfListsSrvInstAW = pfListsSrv;
        this.pingGITOk = ConstantsFor.isPingOK();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
        long delaySch = TimeUnit.MINUTES.toSeconds(ConstantsFor.DELAY) + 3600;
        scheduledThreadPoolExecutor.schedule(pfListsSrvInstAW::makeListRunner, delaySch, TimeUnit.SECONDS);
        messageToUser.info("PfListsSrv. this::makeListRunner", "delay Scheduler", " = " + delaySch);
        AppComponents.threadConfig().executeAsThread(pfListsSrvInstAW::makeListRunner);
        messageToUser.info("Executing as THREAD", "pfListsSrvInstAW::makeListRunner", " = " + pfListsSrvInstAW.toString());
    }

    /**
     Контроллер <a href="/pflists" target=_blank>/pflists</a>
     <p>
     Запись {@link Visitor} ({@link ConstantsFor#getVis(HttpServletRequest)}). <br>
     Определение времени последнего запуска. {@link Properties#getProperty(java.lang.String, java.lang.String)} from {@link #properties} as {@link ConstantsFor#PR_PFSCAN} <br>
     this.{@link #timeOutLong} = последнее сканирование плюс {@link TimeUnit#toMillis(long)} <b>{@link ConstantsFor#DELAY}</b>
     <p>
     Если {@link #pingGITOk}: <br>
     {@link #modSet(Model)} ; <br>
     Если {@link HttpServletRequest#getQueryString()} не {@code null}: <br>
     {@link TaskExecutor#execute(java.lang.Runnable)} - {@link AppComponents#threadConfig()}exec {@link PfListsSrv#makeListRunner()} ; <br>
     Если {@link PfLists#getTimeStampToNextUpdLong()} плюс 1 час к {@link ConstantsFor#DELAY} меньше чем сейчас: <br>
     {@link Model} аттрибуты: ({@link PfListsCtr#ATT_METRIC} , {@code Требуется обновление!} ; ({@link ConstantsFor#ATT_GITSTATS} , )

     @param model    {@link Model}
     @param request  {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @return {@link ConstantsFor#PFLISTS}.html
     @throws UnknownHostException Если {@link PfListsCtr#noPing(org.springframework.ui.Model)}
     */
    @GetMapping("/pflists")
    public String pfBean(@NotNull Model model, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws UnknownHostException {
        ConstantsFor.getVis(request);
        AppComponents.threadConfig().thrNameSet("pfget");

        long lastScan = Long.parseLong(properties.getProperty(ConstantsFor.PR_PFSCAN, "1"));
        @NotNull String refreshRate = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(delayRefInt) * ConstantsFor.ONE_HOUR_IN_MIN);
        timeOutLong = lastScan + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY);
        if (!pingGITOk) {
            noPing(model);
        } else {
            modSet(model);
        }
        if (request.getQueryString() != null) {
            AppComponents.threadConfig().executeAsThread(pfListsSrvInstAW::makeListRunner);
            model.addAttribute(ATT_METRIC, refreshRate);
        }

        long nextUpd = pfListsInstAW.getGitStatsUpdatedStampLong() + TimeUnit.MINUTES.toMillis(DELAY_LOCAL_INT);
        pfListsInstAW.setTimeStampToNextUpdLong(nextUpd);
        if (nextUpd < System.currentTimeMillis()) {
            AppComponents.threadConfig().executeAsThread(pfListsSrvInstAW::makeListRunner);
            model.addAttribute(ATT_METRIC, "Запущено обновление");
            model.addAttribute(ConstantsFor.ATT_GITSTATS, toString());
        } else {
            String msg = String.format("%.02f", (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - pfListsInstAW.getTimeStampToNextUpdLong())) / ConstantsFor.ONE_HOUR_IN_MIN);
            messageToUser.warn(msg);
            model.addAttribute(ATT_METRIC, msg + " min");
        }
        response.addHeader(ConstantsFor.HEAD_REFRESH, refreshRate);
        AppComponents.getOrSetProps(true);
        return ConstantsFor.PFLISTS;
    }

    @PostMapping("/runcom")
    @NotNull
    public String runCommand(@NotNull Model model, @NotNull @ModelAttribute PfListsSrv pfListsSrv) {
        this.pfListsSrvInstAW = pfListsSrv;
        AppComponents.threadConfig().thrNameSet("com.pst");

        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute(ConstantsFor.ATT_TITLE, pfListsSrv.getCommandForNatStr());
        model.addAttribute("PfListsSrv", pfListsSrv);
        model.addAttribute("ok", pfListsSrv.runCom());

        return "ok";
    }

    private static void noPing(Model model) throws UnknownHostException {
        model.addAttribute(ATT_VIPNET, "No ping to srv-git");
        model.addAttribute(ATT_METRIC, LocalTime.now().toString());
        throw new UnknownHostException("srv-git. <font color=\"red\"> NO PING!!!</font>");
    }

    /**
     @param properties {@link ConstantsFor#PROPS}
     */
    private void propUpd(Properties properties) {
        properties.setProperty(ConstantsFor.PR_PFSCAN, pfListsInstAW.getGitStatsUpdatedStampLong() + "");
        messageToUser.info("PfListsCtr.propUpd", "new TForms().fromArray(properties, false)", new TForms().fromArray(properties, false));
        properties.setProperty("meminfo", ConstantsFor.getMemoryInfo());
        properties.setProperty("thr", Thread.activeCount() + "");
    }

    /**
     Установка аттрибутов модели.
     <p>
     PfListsSrv = {@link #pfListsSrvInstAW} <br>
     {@link PfListsCtr#ATT_METRIC} = {@link Date} - {@link PfLists#getTimeStampToNextUpdLong()} <br>
     vipnet = {@link PfLists#getVipNet()} <br>
     tempfull = {@link PfLists#getFullSquid()} <br>
     squidlimited = {@link PfLists#getLimitSquid()} <br>
     squid = {@link PfLists#getStdSquid()} <br>
     nat = {@link PfLists#getPfNat()} <br>
     rules = {@link PfLists#getPfRules()} <br>
     {@link ConstantsFor#ATT_GITSTATS} = {@code gitstatValue "\n" ConstantsFor.getMemoryInfo()} <br>
     {@link ConstantsFor#ATT_FOOTER} = {@link PageFooter#getFooterUtext()}
     <p>
     @param model {@link Model}
     */
    private void modSet(Model model) {
        @NotNull String metricValue = new Date(pfListsInstAW.getTimeStampToNextUpdLong()) + " will be update";
        @NotNull String gitstatValue =
            pfListsInstAW.getInetLog() + "\n" +
                Thread.activeCount() +
                " thr, active\nChange: " +
                (Thread.activeCount() - Long.parseLong(properties.getProperty("thr", "1"))) + "\n" +
                ConstantsFor.getMemoryInfo() + "\n" +
                AppComponents.threadConfig().toString();

        model.addAttribute("PfListsSrv", pfListsSrvInstAW);
        model.addAttribute(ATT_METRIC, metricValue);
        model.addAttribute(ATT_VIPNET, pfListsInstAW.getVipNet());
        model.addAttribute("tempfull", pfListsInstAW.getFullSquid());
        model.addAttribute("squidlimited", pfListsInstAW.getLimitSquid());
        model.addAttribute("squid", pfListsInstAW.getStdSquid());
        model.addAttribute("nat", pfListsInstAW.getPfNat());
        model.addAttribute("rules", pfListsInstAW.getPfRules());
        model.addAttribute(ConstantsFor.ATT_GITSTATS, gitstatValue + "\n" + ConstantsFor.getMemoryInfo() + "\n");
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PfListsCtr{");
        sb.append("ATT_METRIC='").append(ATT_METRIC).append('\'');
        sb.append(", DELAY_LOCAL_INT=").append(DELAY_LOCAL_INT);
        sb.append(", properties=").append(properties.size());
        sb.append(", pfListsInstAW=").append(pfListsInstAW.hashCode());
        sb.append(", pingGITOk=").append(pingGITOk);
        sb.append(", delayRefInt=").append(delayRefInt);
        sb.append(", pfListsSrvInstAW=").append(pfListsSrvInstAW.hashCode());
        sb.append(", timeOutLong=").append(timeOutLong);
        sb.append(", messageToUser=").append(messageToUser.toString());
        sb.append('}');
        return sb.toString();
    }

}
