// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.fileworks.CountSizeOfWorkDir;
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
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.HOURS;


/**
 @see ru.vachok.networker.sysinfo.ServiceInfoCtrlTest
 @since 21.09.2018 (11:33) */
@SuppressWarnings("FeatureEnvy")
@Controller
public class ServiceInfoCtrl {
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ServiceInfoCtrl.class.getSimpleName());
    
    private static final Properties APP_PR = AppComponents.getProps();
    
    private final HTMLGeneration pageFooter = new PageGenerationHelper();
    
    /**
     {@link Visitor}
     */
    @SuppressWarnings({"InstanceVariableMayNotBeInitialized", "InstanceVariableOfConcreteClass"})
    private Visitor visitor;
    
    private boolean authReq;
    
    private static final TForms FORMS = new TForms();
    
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
        ThreadPoolTaskExecutor taskExecutor = AppComponents.threadConfig().getTaskExecutor();
        NetScanService diapazonScan = NetScanService.getInstance(NetScanService.DIAPAZON);
        messageToUser.info(this.getClass().getSimpleName(), "diapazonScan.writeLog()", diapazonScan.writeLog());
        Callable<String> sizeOfDir = new CountSizeOfWorkDir("sizeofdir");
        Future<String> filesSizeFuture = taskExecutor.submit(sizeOfDir);
        model.addAttribute(ModelAttributeNames.HEAD, UsefulUtilities.getAtomicTime() + " atomTime");
        model.addAttribute(ModelAttributeNames.ATT_DIPSCAN, diapazonScan.getExecution());
        String thisDelay = MessageFormat.format("<b>SaveLogsToDB.showInfo(dbIDDiff):  {0} items </b><p>", new SaveLogsToDB().getIDDifferenceWhileAppRunning());
    
        model.addAttribute(ModelAttributeNames.ATT_REQUEST, thisDelay + prepareRequest(request));
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter
            .getFooter(ModelAttributeNames.FOOTER) + "<br><a href=\"/nohup\">" + getJREVers() + "</a>");
        model.addAttribute("mail", percToEnd(additionalDo()));
        model.addAttribute("ping", getClassPath());
        model.addAttribute("urls", makeRunningInfo(filesSizeFuture));
        model.addAttribute("res", makeResValue());
        model.addAttribute("back", request.getHeader(ModelAttributeNames.ATT_REFERER.toLowerCase()));
    }
    
    private Date additionalDo() {
        Callable<Long> callWhenCome = new SpeedChecker();
        Future<Long> whenCome = AppComponents.threadConfig().getTaskExecutor().submit(callWhenCome);
        if (Stats.isSunday()) {
            Stats stats = Stats.getInstance(InformationFactory.STATS_WEEKLY_INTERNET);
            AppComponents.threadConfig().execByThreadConfig((Runnable) stats, "ServiceInfoCtrl.additionalDo");
        }
        Date comeD = new Date();
        try {
            comeD = new Date(whenCome.get(ConstantsFor.DELAY, TimeUnit.SECONDS));
        }
        catch (InterruptedException | ExecutionException | TimeoutException | ArrayIndexOutOfBoundsException e) {
            messageToUser.error(e.getMessage());
        }
        return comeD;
    }
    
    private static @NotNull String makeRunningInfo(@NotNull Future<String> filesSizeFuture) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
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
                .append("<p>")
                .append(filesSizeFuture.get(ConstantsFor.DELAY - 10, TimeUnit.SECONDS)).append("</details></font><br>");
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString().replace("***", "<br>");
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
        stringBuilder.append(FORMS.fromEnum(request.getAttributeNames(), true));
        return stringBuilder.toString();
    }
    
    private String getJREVers() {
        return System.getProperty("java.version");
    }
    
    /**
     Считает время до конца дня.
     <p>
     
     @param timeStart - время старта
     @return время до 17:30 в процентах от 8:30
     */
    private static @NotNull String percToEnd(@NotNull Date timeStart) {
        StringBuilder stringBuilder = new StringBuilder();
        LocalDateTime startDayTime = LocalDateTime.ofEpochSecond(timeStart.getTime() / 1000, 0, ZoneOffset.ofHours(3));
        LocalTime startDay = startDayTime.toLocalTime();
        LocalTime endDay = startDay.plus(9, HOURS);
        
        final int secDayEnd = endDay.toSecondOfDay();
        final int startSec = startDay.toSecondOfDay();
        final int allDaySec = secDayEnd - startSec;
        
        LocalTime localTime = endDay.minusHours(LocalTime.now().getHour());
        localTime = localTime.minusMinutes(LocalTime.now().getMinute());
        localTime = localTime.minusSeconds(LocalTime.now().getSecond());
        boolean workHours = LocalTime.now().isAfter(startDay) && LocalTime.now().isBefore(endDay);
        if (workHours) {
            int toEndDaySec = localTime.toSecondOfDay();
            int diffSec = allDaySec - toEndDaySec;
            float percDay = ((float) toEndDaySec / (((float) allDaySec) / 100));
            stringBuilder
                .append("Работаем ")
                .append(TimeUnit.SECONDS.toMinutes(diffSec));
            stringBuilder
                .append("(мин.). Ещё ")
                .append(String.format("%.02f", percDay))
                .append(" % или ");
        }
        else {
            stringBuilder.append("<b> GO HOME! </b><br>");
        }
        stringBuilder.append(localTime);
        return stringBuilder.toString();
    }
    
    private @NotNull String getClassPath() {
        StringBuilder stringBuilder = new StringBuilder();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        
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
    
    private @NotNull String makeResValue() {
        return new StringBuilder()
            .append(MyCalen.toStringS()).append("<br><br>")
            .append("<b><i>").append(Paths.get(".")).append("</i></b><p><font color=\"orange\">")
            .append(ConstantsNet.getSshMapStr()).append("</font><p>")
                .append(FORMS.fromArray(APP_PR, true)).append("<br>Prefs: ").append(FORMS.fromArray(InitProperties.getUserPref(), true))
            .append("<p>")
            .append(ConstantsFor.HTMLTAG_CENTER).append(FileSystemWorker.readFile(new File("exit.last").getAbsolutePath())).append(ConstantsFor.HTML_CENTER_CLOSE)
            .append("<p>")
            .append("<p><font color=\"grey\">").append(visitsPrevSessionRead()).append("</font>")
            .toString();
    }
    
    private static String visitsPrevSessionRead() {
        List<File> listVisitFiles = new ArrayList<>();
        for (File fileFromList : Objects.requireNonNull(new File(".").listFiles())) {
            if (fileFromList.getName().toLowerCase().contains(UsefulUtilities.getPatternsToDeleteFilesOnStart().get(0))) {
                listVisitFiles.add(fileFromList);
                fileFromList.deleteOnExit();
            }
        }
        ConcurrentMap<String, String> pathFileAsStrMap = readFiles(listVisitFiles);
        List<String> retListStr = new ArrayList<>();
        for (Map.Entry<String, String> entry : pathFileAsStrMap.entrySet()) {
            String pathAsStr = entry.getKey();
            String fileAsStr = entry.getValue();
            try {
                retListStr.add(fileAsStr.split("userId")[0]);
                retListStr.add("<b>" + pathAsStr.split("FtpClientPlus")[1] + "</b>");
            }
            catch (RuntimeException e) {
                retListStr.add(e.getMessage());
            }
        }
        return FORMS.fromArray(retListStr, true);
    }
    
    private static @NotNull ConcurrentMap<String, String> readFiles(List<File> filesToRead) {
        Collections.sort(filesToRead);
        ConcurrentMap<String, String> readiedStrings = new ConcurrentHashMap<>();
        for (File fileRead : filesToRead) {
            String fileReadAsStr = FileSystemWorker.readFile(fileRead.getAbsolutePath());
            readiedStrings.put(fileRead.getAbsolutePath(), fileReadAsStr);
        }
        return readiedStrings;
    }
    
    @GetMapping("/pcoff")
    public void offPC(Model model) throws IOException {
        if (authReq && !UsefulUtilities.thisPC().toLowerCase().contains("home")) {
            String reload = IntoApplication.reloadConfigurableApplicationContext();
            messageToUser.warn(reload);
        }
        else {
//            Runtime.getRuntime().exec(COM_SHUTDOWN_P_F);
            throw new AccessDeniedException("Denied for " + visitor);
        }
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
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ServiceInfoCtrl.class.getSimpleName() + "[\n", "\n]")
            .add("pageFooter = " + pageFooter)
            .add("visitor = " + visitor)
            .add("authReq = " + authReq)
            .toString();
    }
}
