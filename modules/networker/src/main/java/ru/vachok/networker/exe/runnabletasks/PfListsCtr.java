// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.services.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.rmi.UnknownHostException;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 {@link Controller} для {@code /pflists}
 <p>
 <a href="/pflists" target=_blank>Pf Lists</a>
 
 @since 14.11.2018 (15:11) */
@SuppressWarnings({"SameReturnValue", "ClassUnconnectedToPackage"})
@Controller
public class PfListsCtr {
    
    
    /**
     {@link ThreadLocal} {@link String} - {@code metric}
     */
    private static final @NotNull String ATT_METRIC = "metric";
    
    private static final int DELAY_LOCAL_INT = (int) (ConstantsFor.DELAY + ConstantsFor.ONE_HOUR_IN_MIN);
    
    private static final String ATT_VIPNET = "vipnet";
    
    /**
     {@link AppComponents#getProps()}
     */
    private final Properties properties = AppComponents.getProps();
    
    /**
     {@link Random#nextInt(int)} - {@link TimeUnit#toMillis(long)} <b>250</b>
     */
    private final int delayRefInt = new SecureRandom().nextInt((int) TimeUnit.MINUTES.toMillis(250));
    
    /**
     {@link MessageLocal}
     */
    private final MessageToUser messageToUser = new MessageLocal(PfListsCtr.class.getSimpleName());
    
    /**
     {@link PfLists}
     */
    @SuppressWarnings("CanBeFinal")
    private PfLists pfListsInstAW;
    
    /**
     {@link ConstantsFor#isPingOK()}
     */
    @SuppressWarnings("CanBeFinal")
    private boolean pingGITOk;
    
    /**
     {@link PfListsSrv}
     */
    private PfListsSrv pfListsSrvInstAW;
    
    /**
     {@code lastScan плюс TimeUnit.MINUTES.toMillis(15)}
     */
    private long timeOutLong = 1L;
    
    private final ThreadConfig threadConfig = AppComponents.threadConfig();
    
    
    /**
     Public-консттруктор.
     <p>
     
     @param pfLists {@link #pfListsInstAW}
     @param pfListsSrv {@link #pfListsSrvInstAW}
     */
    @Autowired
    public PfListsCtr(PfLists pfLists, PfListsSrv pfListsSrv) {
        this.pfListsInstAW = pfLists;
        this.pfListsSrvInstAW = pfListsSrv;
        this.pingGITOk = ConstantsFor.isPingOK();
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
     
     @param model {@link Model}
     @param request {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @return {@link ConstantsFor#BEANNAME_PFLISTS}.html
     
     @throws UnknownHostException Если {@link PfListsCtr#noPing(org.springframework.ui.Model)}
     */
    @GetMapping("/pflists")
    public String pfBean(@NotNull Model model, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws UnknownHostException {
        threadConfig.thrNameSet("pfget");
    
        long lastScan = Long.parseLong(properties.getProperty(ConstantsFor.PR_PFSCAN, "1"));
        @NotNull String refreshRate = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(delayRefInt) * ConstantsFor.ONE_HOUR_IN_MIN);
        timeOutLong = lastScan + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY);
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        if (!pingGITOk) {
            noPing(model);
        }
        else {
            modSet(model);
        }
        if (request.getQueryString() != null) {
            threadConfig.execByThreadConfig(pfListsSrvInstAW::makeListRunner);
            model.addAttribute(ATT_METRIC, refreshRate);
        }
        long nextUpd = pfListsInstAW.getGitStatsUpdatedStampLong() + TimeUnit.MINUTES.toMillis(DELAY_LOCAL_INT);
        pfListsInstAW.setTimeStampToNextUpdLong(nextUpd);
        if (nextUpd < System.currentTimeMillis()) {
            threadConfig.execByThreadConfig(pfListsSrvInstAW::makeListRunner);
            model.addAttribute(ATT_METRIC, "Запущено обновление");
            model.addAttribute(ConstantsFor.ATT_GITSTATS, toString());
        }
        else {
            String msg = String
                .format("%.02f", (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - pfListsInstAW.getTimeStampToNextUpdLong())) / ConstantsFor.ONE_HOUR_IN_MIN);
            messageToUser.warn(msg);
            model.addAttribute(ATT_METRIC, msg + " min");
        }
        response.addHeader(ConstantsFor.HEAD_REFRESH, refreshRate);
        return ConstantsFor.BEANNAME_PFLISTS;
    }
    
    
    @PostMapping("/runcom")
    public @NotNull String runCommand(@NotNull Model model, @NotNull @ModelAttribute PfListsSrv pfListsSrv) throws UnsupportedOperationException {
        this.pfListsSrvInstAW = pfListsSrv;
        threadConfig.thrNameSet("com.pst");
        
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        model.addAttribute(ConstantsFor.ATT_TITLE, pfListsSrv.getCommandForNatStr());
        model.addAttribute(ConstantsFor.BEANNAME_PFLISTSSRV, pfListsSrv);
        model.addAttribute("ok", pfListsSrv.runCom());
        return "ok";
    }
    
    @Override
    public @NotNull String toString() {
        final @NotNull StringBuilder sb = new StringBuilder("PfListsCtr{");
        sb.append("ATT_METRIC='").append(ATT_METRIC).append('\'');
        sb.append(", DELAY_LOCAL_INT=").append(DELAY_LOCAL_INT);
        sb.append(", properties=").append(properties.size());
        sb.append(", pfListsInstAW=").append(pfListsInstAW.hashCode());
        sb.append(", pingGITOk=").append(pingGITOk);
        sb.append(", delayRefInt=").append(delayRefInt);
        sb.append(", pfListsSrvInstAW=").append(pfListsSrvInstAW.hashCode());
        sb.append(", timeOutLong=").append(timeOutLong);
        sb.append('}');
        return sb.toString();
    }
    
    private void noPing(Model model) throws UnknownHostException {
        model.addAttribute(ATT_VIPNET, "No ping to " + PfListsSrv.getDefaultConnectSrv());
        model.addAttribute(ATT_METRIC, LocalTime.now().toString());
        throw new UnknownHostException(PfListsSrv.getDefaultConnectSrv() + ". <font color=\"red\"> NO PING!!!</font>");
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
     {@link ConstantsFor#ATT_FOOTER} = {@link PageFooter#getFooterUtext()}
     <p>
     {@code gitstatValue} - отображается в последней секции страницы. Показывает: <br>
     {@link PfLists#getInetLog()}, {@link Thread#activeCount()}; {@link Properties#getProperty(java.lang.String, java.lang.String)} {@code "thr", "1"};
     {@link ConstantsFor#getMemoryInfo()}, {@link ThreadConfig#toString()}.
     
     @param model {@link Model}
     */
    private void modSet(Model model) {
        @NotNull String metricValue = new Date(pfListsInstAW.getTimeStampToNextUpdLong()) + " will be update";
        @NotNull String gitstatValue = pfListsInstAW.getInetLog() + "\n" +
                Thread.activeCount() +
                " thr, active\nChange: " +
                (Thread.activeCount() - Long.parseLong(properties.getProperty("thr", "1"))) + "\n" +
                ConstantsFor.getMemoryInfo() + "\n" +
                threadConfig;
        model.addAttribute(ConstantsFor.BEANNAME_PFLISTSSRV, pfListsSrvInstAW);
        model.addAttribute(ATT_METRIC, metricValue);
        model.addAttribute(ATT_VIPNET, pfListsInstAW.getVipNet());
        model.addAttribute("tempfull", pfListsInstAW.getFullSquid());
        model.addAttribute("squidlimited", pfListsInstAW.getLimitSquid());
        model.addAttribute("squid", pfListsInstAW.getStdSquid());
        model.addAttribute("nat", pfListsInstAW.getPfNat());
        model.addAttribute("rules", pfListsInstAW.getPfRules());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
    }
}
