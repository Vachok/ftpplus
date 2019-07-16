// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.accesscontrol.MatrixSRV;
import ru.vachok.networker.accesscontrol.sshactions.Tracerouting;
import ru.vachok.networker.ad.user.MoreInfoWorker;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.net.InfoWorker;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.SimpleCalculator;
import ru.vachok.networker.services.WhoIsWithSRV;
import ru.vachok.networker.sysinfo.VersionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 Контроллер / , /matrix , /git
 
 @since 07.09.2018 (0:35) */
@Controller
public class MatrixCtr {
    
    
    private final ThreadConfig config = AppComponents.threadConfig();
    
    /**
     Логгер
     <p>
     {@link LoggerFactory#getLogger(java.lang.String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixCtr.class.getSimpleName());
    
    /**
     redirect:/matrix
     */
    private static final String REDIRECT_MATRIX = "redirect:/matrix";
    
    /**
     /matrix
     */
    private static final String GET_MATRIX = "/matrix";
    
    /**
     dinner
     */
    private static final String ATT_DINNER = "dinner";
    
    private static String currentProvider = "Unknown yet";
    
    private static String mailIsOk = ConstantsFor.STR_FALSE;
    
    public static void setMailIsOk(String mailIsOk) {
        MatrixCtr.mailIsOk = mailIsOk;
    }
    
    /**
     {@link MatrixSRV}
     */
    private MatrixSRV matrixSRV;
    
    /**
     {@link Visitor}
     */
    private Visitor visitorInst;
    
    /**
     {@link System#currentTimeMillis()}. Время инициализации класса.
     */
    private long metricMatrixStartLong = System.currentTimeMillis();
    
    @Autowired
    public MatrixCtr(MatrixSRV matrixSRV) {
        this.matrixSRV = matrixSRV;
    }
    
    public String getCurrentProvider() {
        return currentProvider;
    }
    
    public void setMatrixSRV(MatrixSRV matrixSRV) {
        this.matrixSRV = matrixSRV;
    }
    
    /**
     Трэйсроуте до 8.8.8.8
     <p>
     С целью определения шлюза по-умолчанию, и соотв. провайдера.
 
     @see AppComponents#sshActs()
     */
    public static void setCurrentProvider() {
        try {
            currentProvider = new Tracerouting().call();
        }
        catch (Exception e) {
            currentProvider = "<br><a href=\"/makeok\">" + e.getMessage() + "</a><br>";
            Thread.currentThread().interrupt();
        }
    }
    
    @GetMapping("/")
    public String getFirst(final HttpServletRequest request, Model model, HttpServletResponse response) {
        this.visitorInst = ConstantsFor.getVis(request);
        InfoWorker infoWorker = new MoreInfoWorker("tv");
        qIsNull(model, request);
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        model.addAttribute(ConstantsFor.ATT_DEVSCAN,
            "Since: " + AppComponents.getUserPref().get(ConstantsFor.FILENAME_PTV, "No date") + infoWorker.getInfoAbout() + currentProvider + "<br>" + mailIsOk);
        response.addHeader(ConstantsFor.HEAD_REFRESH, "120");
        return "starting";
    }
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @PostMapping(GET_MATRIX)
    public String getWorkPosition(@ModelAttribute(ConstantsFor.BEANNAME_MATRIX) MatrixSRV matrixSRV, Model model) {
        this.matrixSRV = matrixSRV;
        String workPos = matrixSRV.getWorkPos();
        if (workPos.toLowerCase().contains("whois:")) {
            return whoisStat(workPos, model);
        }
        else if (workPos.toLowerCase().contains("calc:")) {
            return calculateDoubles(workPos, model);
        }
        else if (Stream.of(ConstantsFor.COMMAND_CALCTIME, ConstantsFor.COMMAND_CALCTIMES, "t:", "T:").anyMatch(s->workPos.toLowerCase().contains(s))) {
            timeStamp(new SimpleCalculator(), model, workPos);
        }
        else {
            return matrixAccess(workPos, model);
        }
        return ConstantsFor.BEANNAME_MATRIX;
    }
    
    /**
     SSH-команда <br> sudo cd /usr/home/ITDept;sudo git instaweb;exit
 
     @param model {@link Model}
     @param request {@link HttpServletRequest}
     @return переадресация на <a href="http://srv-git.eatmeat.ru:1234">http://srv-git.eatmeat.ru:1234</a>
     */
    @GetMapping("/git")
    public String gitOn(Model model, HttpServletRequest request) {
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        this.visitorInst = ConstantsFor.getVis(request);
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        SSHFactory gitOwner = new SSHFactory.Builder(SwitchesWiFi.IPADDR_SRVGIT, "sudo cd /usr/home/ITDept;sudo git instaweb;exit",
            getClass().getSimpleName()).build();
        if (request.getQueryString() != null && request.getQueryString().equalsIgnoreCase(ConstantsFor.COM_REBOOT)) {
            gitOwner = new SSHFactory.Builder(SwitchesWiFi.IPADDR_SRVGIT, "sudo reboot", getClass().getSimpleName()).build();
        }
        String call = gitOwner.call() + "\n" + visitorInst;
        LOGGER.info(call);
        metricMatrixStartLong = System.currentTimeMillis() - metricMatrixStartLong;
        return "redirect:http://srv-git.eatmeat.ru:1234";
    }
    
    /**
     Вывод результата.
     <p>
     1. {@link ConstantsFor#getVis(javax.servlet.http.HttpServletRequest)}. Запишем визит ({@link Visitor}) <br>
     2. {@link MatrixSRV#getWorkPos()}. Пользовательский ввод. <br>
     3. {@link PageFooter#getFooterUtext()}, 4. new {@link PageFooter}, 5. {@link Visitor#toString()}. Компонент модели {@link ConstantsFor#ATT_FOOTER} <br>
     6. {@link MatrixSRV#getCountDB()}. Компонент {@code headtitle}
     <p>
     
     @param request {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @param model {@link Model}
     @return {@link ConstantsFor#BEANNAME_MATRIX}.html
     
     @throws IOException обработка {@link HttpServletResponse#sendError(int, java.lang.String)}
     */
    @GetMapping(GET_MATRIX)
    public String showResults(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        this.visitorInst = ConstantsFor.getVis(request);
        model.addAttribute(ConstantsFor.BEANNAME_MATRIX, matrixSRV);
        String workPos;
        try {
            workPos = matrixSRV.getWorkPos();
        }
        catch (NullPointerException e) {
            response.sendError(139, "");
    
            throw new IllegalStateException("<br>Строка ввода должности не инициализирована!<br>" +
                this.getClass().getName() + "<br>");
        }
        model.addAttribute("workPos", workPos);
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<p>" + visitorInst);
        model.addAttribute("headtitle", matrixSRV.getCountDB() + " позиций   " + TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - ConstantsFor.START_STAMP) + " getUpTime");
        metricMatrixStartLong = System.currentTimeMillis() - metricMatrixStartLong;
        return ConstantsFor.BEANNAME_MATRIX;
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("MatrixCtr{");
        sb.append("currentProvider='").append(currentProvider).append('\'');
        sb.append(", metricMatrixStartLong=").append(new Date(metricMatrixStartLong));
        sb.append('}');
        return sb.toString();
    }
    
    private static String getUserPC(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
    
    private static String whoisStat(String workPos, Model model) {
        WhoIsWithSRV whoIsWithSRV = new WhoIsWithSRV();
        workPos = workPos.split(": ")[1].trim();
        String attributeValue = whoIsWithSRV.whoIs(workPos);
        model.addAttribute(ConstantsFor.ATT_WHOIS, attributeValue);
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute("toHead", new PageFooter().getHeaderUtext());
        return ConstantsFor.BEANNAME_MATRIX;
    }
    
    /**
     Перевод времени из long в {@link Date} и обратно.
     <p>
     1. {@link SimpleCalculator#getStampFromDate(java.lang.String)} метод перевода.
     <p>
 
     @param simpleCalculator {@link SimpleCalculator}
     @param model {@link Model}
     @param workPos {@link MatrixSRV#getWorkPos()}
     @return redirect:/calculate
     */
    @SuppressWarnings("UnusedReturnValue")
    private static String timeStamp(@ModelAttribute SimpleCalculator simpleCalculator, Model model, String workPos) {
        model.addAttribute(ConstantsFor.BEANNAME_CALCULATOR, simpleCalculator);
        model.addAttribute(ATT_DINNER, simpleCalculator.getStampFromDate(workPos));
        return "redirect:/calculate";
    }
    
    
    /**
     Query string отсутствует в реквесте.
     <p>
     1. {@link MatrixCtr#getUserPC(HttpServletRequest)}. Для заголовка страницы. <br> 2. {@link Visitor#toString()} отобразим в {@link #LOGGER} <br> 3. {@link
    VersionInfo#getAppVersion()}. Компонент заголовка. 4. {@link VersionInfo} <br> 5. {@link ConstantsFor#isPingOK()}. Если {@code false} - аттрибут модели {@code ping to srv-git.eatmeat.ru is "
    false} <br> 6. {@link PageFooter#getFooterUtext()}, 7. new {@link PageFooter}. Низ страницы. <br> 8-9 {@link MatrixCtr#getUserPC(HttpServletRequest)} если содержит {@link
    ConstantsFor#HOSTNAME_HOME} или {@code 0:0:0:0}, аттрибут {@link ConstantsFor#ATT_VISIT} - 10. {@link VersionInfo#toString()}, иначе - 11. {@link Visitor#getTimeSpend()}.
     <p>
     
     @param model {@link Model}
     @param request {@link HttpServletRequest}
     */
    private void qIsNull(Model model, HttpServletRequest request) {
        String userPC = getUserPC(request);
        String userIP = userPC + ":" + request.getRemotePort() + "<-" + TimeUnit.SECONDS.toDays(ConstantsFor.getMyTime());
        if (!ConstantsFor.isPingOK()) {
            userIP = "ping to srv-git.eatmeat.ru is " + false;
        }
        model.addAttribute("yourip", userIP);
        model.addAttribute(ConstantsFor.BEANNAME_MATRIX, new MatrixSRV());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        if (getUserPC(request).toLowerCase().contains(ConstantsFor.HOSTNAME_DO213) ||
            getUserPC(request).toLowerCase().contains("0:0:0:0")) {
            model.addAttribute(ConstantsFor.ATT_VISIT, "16.07.2019 (14:48) NOT READY");
        }
    }
    
    
    /**
     Считаем числа с плавающей точкой.
     <p>
     1. {@link AppComponents#simpleCalculator()} <br>
     2. {@link SimpleCalculator#countDoubles(java.util.List)} подсчёт суммы чисел. <br>
     3. {@link MatrixSRV#setWorkPos(java.lang.String)} результат + {@link String} - Dinner price
     <p>
     
     @param workPos {@link MatrixSRV#getWorkPos()}
     @param model {@link Model}
     @return {@link ConstantsFor#BEANNAME_MATRIX}
     */
    private String calculateDoubles(String workPos, Model model) {
        List<Double> list = new ArrayList<>();
        String[] doubles = workPos.split(": ")[1].split(" ");
        for (String aDouble : doubles) {
            list.add(Double.parseDouble(aDouble));
        }
        double v = new AppComponents().simpleCalculator().countDoubles(list);
        String pos = v + " Dinner price";
        matrixSRV.setWorkPos(pos);
        model.addAttribute(ATT_DINNER, pos);
        return ConstantsFor.BEANNAME_MATRIX;
    }
    
    private String matrixAccess(String workPos, Model model) {
        String workPosition = this.matrixSRV.searchAccessPrincipals(workPos);
        this.matrixSRV.setWorkPos(workPosition);
        model.addAttribute("ok", workPosition);
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "ok";
    }
}