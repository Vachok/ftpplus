package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.DiapazonedScan;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.SpeedChecker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Stream;


/**
 Вывод различной сопутствующей информации

 @since 21.09.2018 (11:33) */
@Controller
public class ServiceInfoCtrl {

    /**
     {@link LoggerFactory#getLogger(Class)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInfoCtrl.class.getSimpleName());

    private boolean authReq = false;

    /**
     {@link Visitor}
     */
    private Visitor visitor = null;

    private float getLast() {
        return TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() -
            Long.parseLong(ConstantsFor.getProps().getProperty("lasts", 1544816520000L + ""))) / 60f / 24f;
    }

    private String getJREVers() {
        return System.getProperty("java.version");
    }

    @GetMapping("/serviceinfo")
    public String infoMapping(Model model, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException, ExecutionException, InterruptedException {
        Thread.currentThread().setName("ServiceInfoCtrl.infoMapping");
        ThreadConfig.executeAsThread(new SpeedChecker.ChkMailAndUpdateDB());
        this.visitor = new AppComponents().visitor(request);
        this.authReq = Stream.of("0:0:0:0", "10.10.111", "10.200.213.85", "172.16.20").anyMatch(sP -> request.getRemoteAddr().contains(sP));
        if (authReq) {
            modModMaker(model, request, visitor);
            response.addHeader(ConstantsFor.HEAD_REFRESH, "90");
            return "vir";
        } else {
            throw new AccessDeniedException("Sorry. Denied");
        }
    }

    @GetMapping("/pcoff")
    public void offPC(Model model) throws IOException {
        if (authReq) {
            Runtime.getRuntime().exec(ConstantsFor.COM_SHUTDOWN_P_F);
        } else {
            throw new AccessDeniedException("Denied for " + visitor.toString());
        }
    }

    @GetMapping("/stop")
    public String closeApp() throws AccessDeniedException {
        if (authReq) {
            ThreadConfig.executeAsThread(new ExitApp(ConstantsFor.getUpTime() + " " + ConstantsFor.getMemoryInfo()));
        } else {
            throw new AccessDeniedException("DENY!");
        }
        return "ok";
    }

    private void modModMaker(Model model, HttpServletRequest request, Visitor visitor) throws ExecutionException, InterruptedException {
        this.visitor = ConstantsFor.getVis(request);
        Future<Long> whenCome = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(new SpeedChecker());
        Date comeD = new Date(whenCome.get());
        if (visitor.getSession().equals(request.getSession())) {
            visitor.setClickCounter(visitor.getClickCounter() + 1);
        }
        model.addAttribute(ConstantsFor.ATT_TITLE, getLast() + " (" + getLast() * ConstantsFor.ONE_DAY_HOURS + ")");
        model.addAttribute("mail", ConstantsFor.percToEnd(comeD, 9));
        model.addAttribute("ping", pingGit());
        model.addAttribute("urls", new StringBuilder()
            .append("Запущено - ")
            .append(new Date(ConstantsFor.START_STAMP)).append(ConstantsFor.getUpTime())
            .append(" (<i>rnd delay is ")
            .append(ConstantsFor.DELAY)
            .append("</i>)<br>Точное время: ")
            .append(ConstantsFor.getAtomicTime())
            .append(".<br> Состояние памяти (МБ): <font color=\"#82caff\">")
            .append(ConstantsFor.getMemoryInfo())
            .append("</font><br>")
            .append(DiapazonedScan.getInstance().toString())
            .append("<br>")
            .append(new ThreadConfig().toString())
            .toString());
        model.addAttribute("request", prepareRequest(request));
        model.addAttribute(ConstantsFor.ATT_VISIT, visitor.toString());
        model.addAttribute("res", MyCalen.toStringS() + "<br><br>" + "<b><i>" + AppComponents.versionInfo().toString() + "</i></b>" +
            "<p><font color=\"grey\">" + listFilesToReadStr() + "</font>");
        model.addAttribute("back", request.getHeader(ConstantsFor.ATT_REFERER.toLowerCase()));
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br>" + getJREVers());
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
            .append("<b>").append(request.getHeader("connection")).append(bBr);
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
            .append("<b>").append(request.getHeader("referer")).append(bBr);
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

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private String pingGit() {
        boolean reachable = false;
        try {
            InetAddress byName = InetAddress.getByName(ConstantsFor.SRV_GIT_EATMEAT_RU);
            reachable = byName.isReachable(1000);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        String s = "</b> srv-git.eatmeat.ru.</font> Checked at: <i>";
        String s2 = "</i><br>";
        String s1 = "<b><font color=\"#77ff72\">" + true + s + LocalTime.now() + s2;
        if (reachable) {
            return s1;
        } else {
            return "<b><font color=\"#ff2121\">" + true + s + LocalTime.now() + s2;
        }
    }

    private String listFilesToReadStr() {
        List<File> readUs = new ArrayList<>();
        for (File f : Objects.requireNonNull(new File(".").listFiles())) {
            if (f.getName().toLowerCase().contains(ConstantsFor.STR_VISIT)) {
                readUs.add(f);
            }
        }
        ConcurrentMap<String, String> stringStringConcurrentMap = FileSystemWorker.readFiles(readUs);
        List<String> retListStr = new ArrayList<>();
        //noinspection OverlyLongLambda
        stringStringConcurrentMap.forEach((String x, String y) -> {
            try {
                retListStr.add(y.split("userId")[0]);
                retListStr.add("<b>" + x.split("FtpClientPlus")[1] + "</b>");
            } catch (Exception e) {
                retListStr.add(e.getMessage());
            }
        });
        return new TForms().fromArray(retListStr, true);
    }
}
