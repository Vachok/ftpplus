// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.SpeedChecker;
import ru.vachok.networker.fileworks.CountSizeOfWorkDir;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetPinger;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.services.DBMessenger;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.MyCalen;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.HOURS;


/**
 Вывод различной сопутствующей информации
 
 @since 21.09.2018 (11:33) */
@Controller
public class ServiceInfoCtrl {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(ServiceInfoCtrl.class.getSimpleName());
    
    private final ThreadConfig threadConfig = AppComponents.threadConfig();
    
    private boolean authReq;
    
    /**
     {@link Visitor}
     */
    private Visitor visitor;
    
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
     
     @throws AccessDeniedException если не {@link ConstantsFor#getPcAuth(HttpServletRequest)}
     @throws ExecutionException запуск {@link #modModMaker(Model, HttpServletRequest, Visitor)}
     @throws InterruptedException запуск {@link #modModMaker(Model, HttpServletRequest, Visitor)}
     */
    @GetMapping("/serviceinfo")
    public String infoMapping(Model model, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException, ExecutionException, InterruptedException {
        threadConfig.thrNameSet("info");
        messageToUser.warn(getClass().getSimpleName(), "netPinger minutes", " = " + netPinger());
        visitor = new AppComponents().visitor(request);
        threadConfig.execByThreadConfig(new SpeedChecker());
        this.authReq = Stream.of("0:0:0:0", "127.0.0.1", "10.10.111", "10.200.213.85", "172.16.20", "10.200.214.80", "192.168.13.143")
            .anyMatch(sP->request.getRemoteAddr().contains(sP));
        if (authReq) {
            modModMaker(model, request, visitor);
            response.addHeader(ConstantsFor.HEAD_REFRESH, "90");
            return "vir";
        }
        else {
            throw new AccessDeniedException("Sorry. Denied, for you: " + request.getRemoteAddr());
        }
    }
    
    @GetMapping("/pcoff")
    public void offPC(Model model) throws IOException {
        if (authReq) {
            Runtime.getRuntime().exec(ConstantsFor.COM_SHUTDOWN_P_F);
        }
        else {
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
                new DBMessenger(getClass().getSimpleName()).infoNoTitles(this.getClass().getSimpleName() + " " + e.getMessage() + " :(((");
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
        final StringBuilder sb = new StringBuilder("ServiceInfoCtrl{");
        sb.append("authReq=").append(authReq);
        sb.append(", visitor=").append(visitor);
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Считает время до конца дня.
     <p>
     
     @param timeStart - время старта
     @param amountH - сколько часов до конца
     @return время до 17:30 в процентах от 8:30
     */
    private static String percToEnd(Date timeStart, long amountH) {
        StringBuilder stringBuilder = new StringBuilder();
        LocalDateTime startDayTime = LocalDateTime.ofEpochSecond(timeStart.getTime() / 1000, 0, ZoneOffset.ofHours(3));
        LocalTime startDay = startDayTime.toLocalTime();
        LocalTime endDay = startDay.plus(amountH, HOURS);
    
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
    
    @Scope(ConstantsFor.SINGLETON)
    private static NetPinger netPinger() {
        return new NetPinger();
    }
    
    private void modModMaker(Model model, HttpServletRequest request, Visitor visitorParam) throws ExecutionException, InterruptedException {
        this.visitor = ConstantsFor.getVis(request);
        this.visitor = visitorParam;
        
        Callable<String> sizeOfDir = new CountSizeOfWorkDir("sizeofdir");
        Callable<Long> callWhenCome = new SpeedChecker();
        Callable<String> filesWithSize;
        Future<Long> whenCome = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(callWhenCome);
        Future<String> filesSizeFuture = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(sizeOfDir);
        Date comeD = new Date(whenCome.get());
        String resValue = new StringBuilder()
            .append(MyCalen.toStringS()).append("<br><br>")
            .append("<b><i>").append(AppComponents.versionInfo()).append("</i></b><p><font color=\"orange\">")
            .append(ConstantsNet.getSshMapStr()).append("</font><p>")
            .append(new AppInfoOnLoad()).append(" ").append(AppInfoOnLoad.class.getSimpleName()).append("<p>")
            .append(new TForms().fromArray(AppComponents.getProps(), true))
            .append("<p>")
            .append("<center>").append(FileSystemWorker.readFile(new File("exit.last").getAbsolutePath())).append("</center>").append("<p>")
            .append("<p><font color=\"grey\">").append(listFilesToReadStr()).append("</font>")
            .toString();
        
        model.addAttribute(ConstantsFor.ATT_TITLE, getLast() + " " + pingDO0213());
        model.addAttribute("mail", percToEnd(comeD, 9));
        model.addAttribute("ping", pingGit());
        model.addAttribute("urls", new StringBuilder()
            .append("Запущено - ")
            .append(new Date(ConstantsFor.START_STAMP)).append(ConstantsFor.getUpTime())
            .append(" (<i>rnd delay is ")
            .append(ConstantsFor.DELAY).append(" : ")
            .append(String.format("%.02f",
                (float) (ConstantsFor.getAtomicTime() - ConstantsFor.START_STAMP) / TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY))).append(" delays)</i>")
            .append(".<br> Состояние памяти (МБ): <font color=\"#82caff\">")
            .append(ConstantsFor.getMemoryInfo()).append("<details><summary> disk usage by program: </summary>").append(filesSizeFuture.get()).append("</details><br>")
            .append("</font><br>")
            .append(AppComponents.diapazonedScan())
            .append("<br>")
            .append(AppComponents.threadConfig())
            .toString());
        model.addAttribute("request", prepareRequest(request));
        model.addAttribute(ConstantsFor.ATT_VISIT, visitorParam.toString());
        model.addAttribute("res", resValue);
        model.addAttribute("back", request.getHeader(ConstantsFor.ATT_REFERER.toLowerCase()));
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br><a href=\"/nohup\">" + getJREVers() + "</a>");
    }
    
    private BigDecimal getLast() {
        long toSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - Long.parseLong(AppComponents.getProps()
            .getProperty(ConstantsFor.PR_LASTS, "1543958100000")));
        float val = ConstantsFor.ONE_HOUR_IN_MIN * ConstantsFor.ONE_HOUR_IN_MIN * ConstantsFor.ONE_DAY_HOURS;
    
        return BigDecimal.valueOf(toSeconds).divide(BigDecimal.valueOf(val), 2, RoundingMode.HALF_DOWN);
    }
    
    private String getJREVers() {
        return System.getProperty("java.version");
    }
    
    private String prepareRequest(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<center><h3>Заголовки</h3></center>");
        String bBr = "</b><br>";
        stringBuilder
            .append("HOST: ")
            .append("<b>").append(request.getHeader("host")).append(bBr);
        stringBuilder
            .append("CONNECTION: ")
            .append("<b>").append(request.getHeader(ConstantsNet.STR_CONNECTION)).append(bBr);
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
        stringBuilder.append(new TForms().fromEnum(request.getAttributeNames(), true));
        return stringBuilder.toString();
    }
    
    private static String listFilesToReadStr() {
        List<File> readUs = new ArrayList<>();
        for (File f : Objects.requireNonNull(new File(".").listFiles())) {
            if (f.getName().toLowerCase().contains(ConstantsFor.getStringsVisit()[0])) {
                readUs.add(f);
                f.deleteOnExit();
            }
        }
        ConcurrentMap<String, String> stringStringConcurrentMap = FileSystemWorker.readFiles(readUs);
        List<String> retListStr = new ArrayList<>();
        stringStringConcurrentMap.forEach((String x, String y)->{
            try {
                retListStr.add(y.split("userId")[0]);
                retListStr.add("<b>" + x.split("FtpClientPlus")[1] + "</b>");
            }
            catch (Exception e) {
                retListStr.add(e.getMessage());
            }
        });
        return new TForms().fromArray(retListStr, true);
    }
    
    private String pingGit() {
        boolean reachable = false;
        try {
            InetAddress byName = InetAddress.getByName(ConstantsFor.HOSTNAME_SRVGITEATMEATRU);
            reachable = byName.isReachable(200);
        }
        catch (IOException e) {
            messageToUser.errorAlert("ServiceInfoCtrl", "pingGit", e.getMessage());
        }
        String s = "</b> srv-git.eatmeat.ru.</font> Checked at: <i>";
        String s2 = "</i><br>";
        String s1 = "<b><font color=\"#77ff72\">" + true + s + LocalTime.now() + s2;
        if (reachable) {
            return s1;
        }
        else {
            return "<b><font color=\"#ff2121\">" + true + s + LocalTime.now() + s2;
        }
    }
    
    private String pingDO0213() {
        try {
            InetAddress nameHost = InetAddress.getByName(OtherKnownDevices.DO0213_KUDR);
            return nameHost.getHostName().replace(ConstantsFor.DOMAIN_EATMEATRU, "") + " is " + nameHost.isReachable((int) (ConstantsFor.DELAY * 3));
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
            return e.getMessage();
        }
    }
}
