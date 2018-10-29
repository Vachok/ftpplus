package ru.vachok.networker.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.*;
import ru.vachok.networker.logic.SSHFactory;
import ru.vachok.networker.services.MatrixSRV;
import ru.vachok.networker.services.VisitorSrv;
import ru.vachok.networker.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 @since 07.09.2018 (0:35) */
@Controller
public class MatrixCtr {

    /*Fields*/

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String MATRIX_STRING_NAME = "matrix";

    private static final String REDIRECT_MATRIX = "redirect:/matrix";

    private MatrixSRV matrixSRV;

    private VisitorSrv visitorSrv;

    private Visitor visitor;

    private VersionInfo versionInfo;

    private long metricMatrixStart = System.currentTimeMillis();

    private static final String FOOTER_NAME = "footer";

    @Autowired
    public MatrixCtr(VisitorSrv visitorSrv, Visitor visitor, VersionInfo versionInfo) {
        this.visitorSrv = visitorSrv;
        this.visitor = visitor;
        this.versionInfo = versionInfo;
    }

    /**
     <h2>Начальная страница</h2>
     <b>matrix.html</b>

     @param request  {@link HttpServletRequest}
     @param model    {@link Model}
     @param response {@link HttpServletResponse}
     @return название файла html, в который помещаем модель.
     */
    @GetMapping("/")
    public String getFirst(final HttpServletRequest request, Model model, HttpServletResponse response) {
        String userPC = ConstantsFor.getUserPC(request);
        boolean pcAuth = ConstantsFor.getPcAuth(request);
        if (request.getQueryString() != null) {
            String queryString = request.getQueryString();
            if (queryString.equalsIgnoreCase("eth") && pcAuth) {
                lastLogsGetter(model);
                model.addAttribute(FOOTER_NAME, new PageFooter().getFooterUtext());
                metricMatrixStart = System.currentTimeMillis() - metricMatrixStart;
                return "logs";
            }
        } else {
            try {
                visitorSrv.makeVisit(request);
            } catch (Exception ignore) {
                //
            }
            String userIP = userPC + ":" + request.getRemotePort() + "<-" + response.getStatus();
            model.addAttribute("yourip", userIP);
            model.addAttribute(MATRIX_STRING_NAME, new MatrixSRV());
            model.addAttribute(FOOTER_NAME, new PageFooter().getFooterUtext());
            if (ConstantsFor.getUserPC(request).toLowerCase().contains(ConstantsFor.NO0027) ||
                ConstantsFor.getUserPC(request).toLowerCase().contains("0:0:0:0")) {
                model.addAttribute("visit", versionInfo.toString());
            } else {
                model.addAttribute("visit", visitor.getTimeSt() + " timestamp");
            }
        }
        return "starting";
    }

    private void lastLogsGetter(Model model) {
        Map<String, String> vachokEthosdistro = new AppComponents().getLastLogs();
        String logsFromDB = new TForms().fromArray(vachokEthosdistro);
        model.addAttribute("logdb", logsFromDB);
        model.addAttribute("starttime", new Date(ConstantsFor.START_STAMP));
        model.addAttribute(FOOTER_NAME, new PageFooter().getFooterUtext());
        model.addAttribute("title", metricMatrixStart);
    }

    @PostMapping("/matrix")
    public String getWorkPosition(@ModelAttribute(MATRIX_STRING_NAME) MatrixSRV matrixSRV, BindingResult result, Model model) {
        this.matrixSRV = matrixSRV;
        String workPos = matrixSRV.getWorkPos();
        if (workPos.toLowerCase().contains("whois:")) return whois(workPos, model);
        else if (workPos.toLowerCase().contains("calc:")) return calculateDoubles(workPos, model);
        else return matrixAccess(workPos);
    }

    @GetMapping("/matrix")
    public String showResults(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        new Thread(() -> {
            try {
                visitorSrv.makeVisit(request);
            } catch (IllegalArgumentException | NoSuchMethodException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }).start();
        model.addAttribute(MATRIX_STRING_NAME, matrixSRV);
        String workPos;
        try {
            workPos = matrixSRV.getWorkPos();
        } catch (NullPointerException e) {
            response.sendError(139, "");

            throw new IllegalStateException("<br>Строка ввода должности не инициализирована!<br>" +
                this.getClass().getName() + "<br>");
        }
        model.addAttribute("workPos", workPos);
        model.addAttribute(FOOTER_NAME, new PageFooter().getFooterUtext());
        model.addAttribute("headtitle", matrixSRV.getCountDB() + " позиций   " + TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - ConstantsFor.START_STAMP) + " upTime");
        metricMatrixStart = System.currentTimeMillis() - metricMatrixStart;
        return MATRIX_STRING_NAME;
    }

    /**
     <h2>SSH-команда</h2>
     sudo cd /usr/home/ITDept;sudo git instaweb;exit

     @param model   {@link Model}
     @param request {@link HttpServletRequest}
     @return переадресация на <a href="http://srv-git.eatmeat.ru:1234">http://srv-git.eatmeat.ru:1234</a>
     */
    @GetMapping("/git")
    public String gitOn(Model model, HttpServletRequest request) {
        try {
            visitorSrv.makeVisit(request);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        SSHFactory gitOner = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo cd /usr/home/ITDept;sudo git instaweb;exit").build();
        if (request.getQueryString() != null && request.getQueryString().equalsIgnoreCase("reboot")) {
            gitOner = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo reboot").build();
        }
        LOGGER.info(gitOner.call());
        metricMatrixStart = System.currentTimeMillis() - metricMatrixStart;
        return "redirect:http://srv-git.eatmeat.ru:1234";
    }

    private String whois(String workPos, Model model) {
        try {
            WhoIsWithSRV whoIsWithSRV = new WhoIsWithSRV();
            workPos = workPos.split(": ")[1];
            String attributeValue = whoIsWithSRV.whoIs(workPos);
            model.addAttribute("whois", attributeValue);
            model.addAttribute(FOOTER_NAME, new PageFooter().getFooterUtext());
        } catch (ArrayIndexOutOfBoundsException e) {
            model.addAttribute("whois", workPos + "<p>" + e.getMessage());
            return MATRIX_STRING_NAME;
        }
        metricMatrixStart = System.currentTimeMillis() - metricMatrixStart;
        return MATRIX_STRING_NAME;
    }

    private String calculateDoubles(String workPos, Model model) {
        List<Double> list = new ArrayList<>();
        String[] doubles = workPos.split(": ")[1].split(" ");
        for (String aDouble : doubles) {
            list.add(Double.parseDouble(aDouble));
        }
        double v = new AppComponents().simpleCalculator().countDoubles(list);
        String pos = v + " Dinner price";
        matrixSRV.setWorkPos(pos);
        model.addAttribute("dinner", pos);
        return MATRIX_STRING_NAME;
    }

    private String matrixAccess(String workPos) {
        String workPosition = this.matrixSRV.getWorkPosition(String
            .format("select * from matrix where Doljnost like '%%%s%%';", workPos));
        this.matrixSRV.setWorkPos(workPosition);
        LOGGER.info(workPosition);
        return MATRIX_STRING_NAME;
    }

    private String resoString() {
        StringBuilder stringBuilder = new StringBuilder();
        ResoCache resoCache = ResoCache.getResoCache();
        stringBuilder.append(resoCache.exists()).append(" resources");
        return stringBuilder.toString();
    }
}