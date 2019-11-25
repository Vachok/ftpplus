// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.controller.ErrCtr;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.exe.runnabletasks.SpeedChecker;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.info.stats.Stats;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.AccessDeniedException;
import java.text.MessageFormat;
import java.time.*;
import java.util.Date;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.HOURS;


/**
 @see ServiceInfoCtrlTest
 @since 21.09.2018 (11:33) */
@SuppressWarnings("FeatureEnvy")
@Controller
public class ServiceInfoCtrl {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ServiceInfoCtrl.class.getSimpleName());
    
    private final HTMLGeneration pageFooter = new PageGenerationHelper();
    
    /**
     {@link Visitor}
     */
    @SuppressWarnings({"InstanceVariableMayNotBeInitialized", "InstanceVariableOfConcreteClass"})
    private Visitor visitor;
    
    private boolean authReq;
    
    public ServiceInfoCtrl() {
    }
    
    @Contract(pure = true)
    protected ServiceInfoCtrl(Visitor visitor) {
        this.visitor = visitor;
    }
    
    /**
     GetMapping /serviceinfo
     <p>
     Записываем {@link Visitor}. <br>
     Выполним как трэд - new {@link SpeedChecker}. <br>
     Если ПК авторизован - вернуть {@code vir.html}, иначе - throw new {@link AccessDeniedException}
     
     @param model {@link Model}
     @param request {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @return vir.html
     
     @throws AccessDeniedException если не {@link ErrCtr#getPcAuth(HttpServletRequest)}
     */
    @GetMapping("/serviceinfo")
    public String infoMapping(@NotNull Model model, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException {
        model.addAttribute(ModelAttributeNames.TITLE, UsefulUtilities.getTotalCPUTimeInformation() + " total CPU");
        this.authReq = Stream.of("0:0:0:0", "127.0.0.1", "10.10.111", "10.200.213.85", "172.16.20", "10.200.214.80")
                .anyMatch(sP->request.getRemoteAddr().contains(sP));
        visitor = new AppComponents().visitor(request);
        if (authReq) {
            modModMaker(model, request, visitor);
            response.addHeader(ConstantsFor.HEAD_REFRESH, "88");
            return "vir";
        }
        else {
            throw new AccessDeniedException("Sorry. Denied, for you: " + request.getRemoteAddr());
        }
    }
    
    private void modModMaker(@NotNull Model model, HttpServletRequest request, Visitor visitorParam) {
        this.visitor = UsefulUtilities.getVis(request);
        this.visitor = visitorParam;
        NetScanService diapazonScan = NetScanService.getInstance(NetScanService.DIAPAZON);
        String thisDelay = MessageFormat.format("<b>SaveLogsToDB.showInfo(dbIDDiff):  {0} items </b><p>", new SaveLogsToDB().getIDDifferenceWhileAppRunning());
        
        model.addAttribute(ModelAttributeNames.HEAD, UsefulUtilities.getAtomicTime() + " atomTime");
        model.addAttribute(ModelAttributeNames.ATT_DIPSCAN, diapazonScan.getExecution());
        
        model.addAttribute(ModelAttributeNames.ATT_REQUEST, thisDelay + prepareRequest(request));
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter
                .getFooter(ModelAttributeNames.FOOTER) + "<br><a href=\"/nohup\">" + getJREVers() + "</a>");
        model.addAttribute("mail", percToEnd(getDateWhenCome()));
        model.addAttribute("ping", getClassPath());
        model.addAttribute("urls", makeRunningInfo());
        model.addAttribute("res", makeResValue());
        model.addAttribute("back", request.getHeader(ModelAttributeNames.ATT_REFERER.toLowerCase()));
    }
    
    private @NotNull String prepareRequest(@NotNull HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<center><h3>Заголовки</h3></center>");
        String bBr = "</b><br>";
        stringBuilder
                .append("HOST: ")
                .append("<b>").append(request.getHeader("host")).append(bBr);
        stringBuilder
                .append("upgrade-insecure-requests: ".toUpperCase())
                .append("<b>").append(request.getHeader("upgrade-insecure-requests")).append(bBr);
        stringBuilder
                .append("user-agent: ".toUpperCase())
                .append("<b>").append(request.getHeader("user-agent")).append(bBr);
        stringBuilder
                .append("ACCEPT: ")
                .append("<b>").append(request.getHeader("accept")).append(bBr);
        stringBuilder
                .append("referer: ".toUpperCase())
                .append("<b>").append(request.getHeader(ConstantsFor.HEAD_REFERER)).append(bBr);
        stringBuilder
                .append("accept-encoding: ".toUpperCase())
                .append("<b>").append(request.getHeader("accept-encoding")).append(bBr);
        stringBuilder
                .append("accept-language: ".toUpperCase())
                .append("<b>").append(request.getHeader("accept-language")).append(bBr);
        stringBuilder
                .append("cookie: ".toUpperCase())
                .append("<b>").append(request.getHeader("cookie")).append(bBr);
        
        stringBuilder.append("<center><h3>Атрибуты</h3></center>");
        stringBuilder.append(AbstractForms.fromEnum(request.getAttributeNames()).replace("<br>", "\n"));
        return stringBuilder.toString();
    }
    
    private String getJREVers() {
        return System.getProperty("java.version");
    }
    
    /**
     Считает время до конца дня.
     <p>
 
     @param dateWhenCome - время старта
     @return время до 17:30 в процентах от 8:30
     */
    private static @NotNull String percToEnd(@NotNull Date dateWhenCome) {
        StringBuilder stringBuilder = new StringBuilder();
        LocalDateTime startDayTime = LocalDateTime.ofEpochSecond(dateWhenCome.getTime() / 1000, 0, ZoneOffset.ofHours(3));
        LocalTime startDay = startDayTime.toLocalTime();
        LocalTime endDay = startDay.plus(9, HOURS);
    
        LocalTime toEnd = endDay.minusHours(LocalTime.now().getHour());
        toEnd = toEnd.minusMinutes(LocalTime.now().getMinute());
        toEnd = toEnd.minusSeconds(LocalTime.now().getSecond());
        boolean workHours = LocalTime.now().isAfter(startDay) && LocalTime.now().isBefore(endDay);
        if (workHours) {
            stringBuilder.append(parseWorkHours(startDay, toEnd));
        }
        else {
            stringBuilder.append("<b> GO HOME! </b><br>");
        }
        stringBuilder.append(toEnd);
        return stringBuilder.toString();
    }
    
    private Date getDateWhenCome() {
        if (Stats.isSunday()) {
            Stats stats = Stats.getInstance(InformationFactory.STATS_WEEKLY_INTERNET);
            AppConfigurationLocal.getInstance().execute((Runnable) stats, 150);
        }
        return new Date(new SpeedChecker().call());
    }
    
    private static @NotNull String parseWorkHours(LocalTime startDay, LocalTime toEnd) {
        LocalTime endDay = startDay.plus(9, HOURS);
        final int secDayEnd = endDay.toSecondOfDay();
        final int startSec = startDay.toSecondOfDay();
        StringBuilder stringBuilder = new StringBuilder();
        final int allDaySec = secDayEnd - startSec;
        
        int toEndDaySec = toEnd.toSecondOfDay();
        int diffSec = allDaySec - toEndDaySec;
        float percDay = ((float) toEndDaySec / (((float) allDaySec) / 100));
        stringBuilder
            .append("Работаем ")
            .append(TimeUnit.SECONDS.toMinutes(diffSec));
        stringBuilder
            .append("(мин.). Ещё ")
            .append(String.format("%.02f", percDay))
            .append(" % или ");
        return stringBuilder.toString();
    }
    
    private @NotNull String getClassPath() {
        StringBuilder stringBuilder = new StringBuilder();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        stringBuilder.append(AbstractForms.fromArray(InitProperties.getTheProps()).replace("\n", "<br>"));
        stringBuilder.append(AbstractForms.fromArray(InitProperties.getUserPref()).replace("\n", "<br>"));
        stringBuilder.append("ClassPath {<br>");
        stringBuilder.append(runtimeMXBean.getClassPath().replace(";", "<br>")).append(" }<p>");
        stringBuilder.append("BootClassPath {<br>");
        try {
            stringBuilder.append(runtimeMXBean.getBootClassPath().replace("\n", "<br>")).append("}<p>");
        }
        catch (UnsupportedOperationException e) {
            stringBuilder.append(e.getMessage().replace("\n", "<br>")).append(" }<p>");
        }
        stringBuilder.append("LibraryPath {<br>");
        stringBuilder.append(runtimeMXBean.getLibraryPath().replace(";", "<br>")).append(" }<p>");
        
        return stringBuilder.toString();
    }
    
    private static @NotNull String makeRunningInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Запущено - ")
                .append(new Date(ConstantsFor.START_STAMP))
                .append(UsefulUtilities.getUpTime())
                .append(" (<i>rnd delay is ")
                .append(ConstantsFor.DELAY)
                .append(" : ")
                .append(String.format("%.02f", (float) (UsefulUtilities.getAtomicTime() - ConstantsFor.START_STAMP) / TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)))
                .append(" delays)</i>")
                .append(".<br> Состояние памяти (МБ): <font color=\"#82caff\">")
                .append(UsefulUtilities.getRunningInformation())
                .append("<details><summary> disk and threads time used by program: </summary>").append("<br>").append(AppComponents.threadConfig().getAllThreads())
                .append("<p>").append("</details></font><br>");
        return stringBuilder.toString().replace("***", "<br>");
    }
    
    private @NotNull String makeResValue() {
        return new StringBuilder()
                .append(MyCalen.toStringS()).append("<br><br>")
                .append("<b><i>").append("</i></b><p><font color=\"orange\">")
                .append(ConstantsNet.getSshMapStr()).append("</font><p>")
                .append(ConstantsFor.HTMLTAG_CENTER).append(FileSystemWorker.readFile(new File("exit.last").getAbsolutePath())).append(ConstantsFor.HTML_CENTER_CLOSE)
                .append("<p>")
                .toString();
    }
    
    @GetMapping("/pcoff")
    public void offPC(Model model) throws IOException {
        if (authReq) {
            Runtime.getRuntime().exec("SHUTDOWN /P /F");
        }
        else {
            throw new AccessDeniedException("Denied for " + visitor);
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ServiceInfoCtrl.class.getSimpleName() + "[\n", "\n]")
                .add("pageFooter = " + pageFooter)
                .add("visitor = " + visitor)
                .add("authReq = " + authReq)
                .toString();
    }
    
    @GetMapping("/stop")
    public String closeApp(HttpServletRequest request) throws AccessDeniedException {
        if (authReq) {
            try {
                new ExitApp(getClass().getSimpleName()).run();
            }
            catch (RuntimeException e) {
                messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".closeApp", e));
                System.exit(ConstantsFor.EXIT_STATUSBAD / 3);
            }
        }
        else {
            throw new AccessDeniedException("DENY for " + request.getRemoteAddr());
        }
        return "ok";
    }
}
