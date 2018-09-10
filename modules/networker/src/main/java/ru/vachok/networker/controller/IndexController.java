package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.DBMessenger;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.beans.PfLists;
import ru.vachok.networker.config.AppComponents;
import ru.vachok.networker.logic.ssh.ListInternetUsers;
import ru.vachok.networker.services.PfListsSrv;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * The type Index controller.
 */
@Controller
public class IndexController {

    /*Fields*/
    private static final Map<String, String> SHOW_ME = new ConcurrentHashMap<>();

    private static final String SOURCE_CLASS = IndexController.class.getName();

    private static Logger logger = AppComponents.getLogger();

    private MessageToUser messageToUser = new DBMessenger();


    /**
     * Map to show map.
     *
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @return the map
     * @throws IOException the io exception
     */
    @RequestMapping("/docs")
    public Map<String, String> mapToShow(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        SHOW_ME.put("addr", httpServletRequest.getRemoteAddr());
        SHOW_ME.put("host", httpServletRequest.getRequestURL().toString());
        SHOW_ME.forEach((x, y) -> messageToUser.info(this.getClass().getSimpleName(), x, y));
        SHOW_ME.put("status", httpServletResponse.getStatus() + " " + httpServletResponse.getBufferSize() + " buff");
        String s = httpServletRequest.getQueryString();
        if (s != null) {
            SHOW_ME.put(this.toString(), s);
            if (s.contains("go")) {
                httpServletResponse.sendRedirect("http://ftpplus.vachok.ru/docs");
            }
        }
        return SHOW_ME;
    }

    /**
     * Addr in locale stream.
     *
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @throws IOException the io exception
     */
    @GetMapping("/rnd")
    @ResponseBody
    public void addrInLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Model model) {
        scheduleAns();
        List<String> namesFile = new ArrayList<>();
        String re = "redirect:https://vachok.testquality.com/project/3260/plan/6672/test/86686";
        Cookie cooki = new Cookie("hi", re);
        httpServletResponse.addCookie(cooki);
        byte[] bs = new byte[0];
        try (ServletInputStream in = httpServletRequest.getInputStream()) {

            while (in.isReady()) {
                int read = in.read(bs);
                String msg = read + " bytes were read";
                logger.info(msg);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        messageToUser.info("HTTP Servlets Controller", httpServletRequest.getServletPath() + re, "1 КБ resp: " + new String(bs, StandardCharsets.UTF_8));
        String s = LocalDateTime.of(2018, 10, 14, 7, 0).format(DateTimeFormatter.ofPattern("dd/MM/yy"));
        Map<String, String> getEnv = System.getenv();
        getEnv.forEach((x, y) -> namesFile.add(x + "\n" + y));
        namesFile.add(re);
        namesFile.add(new String(bs, StandardCharsets.UTF_8));
        namesFile.add(s);
        namesFile.add(httpServletRequest.toString());
        namesFile.add(httpServletRequest.getSession().getServletContext().getServerInfo());
        namesFile.add(httpServletRequest.getSession().getServletContext().getServletContextName());
        namesFile.add(httpServletRequest.getSession().getServletContext().getVirtualServerName());
        namesFile.add(httpServletRequest.getSession().getServletContext().getContextPath());
        namesFile.add(Arrays.toString(httpServletResponse.getHeaderNames().toArray()));
        for (String name : namesFile) {
            model.addAttribute("virTxt", name);
        }
        model.addAttribute("attr", getAttr(httpServletRequest));
    }

    private void scheduleAns() {
        ScheduledExecutorService executorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        Runnable runnable = () -> {
            MessageToUser m = new DBMessenger();
            float upTime = (float) (System.currentTimeMillis() - ConstantsFor.START_STAMP) /
                TimeUnit.DAYS.toMillis(1);
            m.info(SOURCE_CLASS, "UPTIME", upTime + " days");
        };
        int delay = new Random().nextInt((int) TimeUnit.MINUTES.toSeconds(1) / 3);
        int init = new Random().nextInt((int) TimeUnit.MINUTES.toSeconds(1));
        executorService.scheduleWithFixedDelay(runnable, init, delay, TimeUnit.MINUTES);
        String msg = runnable + " " + init + " init ," + delay + " delay";
        logger.info(msg);
    }

    @GetMapping("/pf")
    public String indexModel(HttpServletRequest request, HttpServletResponse response, Model model) {
        long metricStart = System.currentTimeMillis();
        Map<String, String> sshResults = new ListInternetUsers().call();
        List<String> commandsSSH = new ListInternetUsers().getCommand();
        for (String s : commandsSSH) {
            new Thread(() -> {
                String sshRes = sshResults.get(s);
                model.addAttribute(s.split("cat /etc/pf/")[1], sshRes);
            }).start();
        }
        boolean b = IntoApplication.dataSender(request, SOURCE_CLASS);
        model.addAttribute("dbsend", b + " db");
        String msg = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - metricStart) + " elapsed sec.";
        logger.info(msg);
        MessageToUser mailMSG = new ESender("143500@gmail.com");
        new Thread(() -> mailMSG.info(SOURCE_CLASS,
            ((float) (TimeUnit.MILLISECONDS
                .toSeconds(System
                    .currentTimeMillis() - ConstantsFor.START_STAMP)) / 60f) +
                " min uptime", msg)).start();
        return "index";
    }

    @GetMapping("/pfbean")
    public String pfBean(Model model, @ModelAttribute("PfLists") PfLists pfLists) {
        model = new PfListsSrv(pfLists).pfListsUpd(model);
        return "index";
    }

    private String getAttr(HttpServletRequest request) {
        Enumeration<String> attributeNames = request.getServletContext().getAttributeNames();
        StringBuilder stringBuilder = new StringBuilder();
        while (attributeNames.hasMoreElements()) {
            stringBuilder.append(attributeNames.nextElement());
            stringBuilder.append("<p>");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
