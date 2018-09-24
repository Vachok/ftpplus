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
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.logic.SSHFactory;
import ru.vachok.networker.services.DataBasesSRV;
import ru.vachok.networker.services.MatrixSRV;
import ru.vachok.networker.services.VisitorSrv;
import ru.vachok.networker.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @since 07.09.2018 (0:35)
 */
@Controller
public class MatrixCtr {

    /*Fields*/

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String MATRIX_STRING_NAME = "matrix";

    private MatrixSRV matrixSRV;

    private VisitorSrv visitorSrv;

    private Visitor visitor;

    private long metricMatrixStart = System.currentTimeMillis();

    private DataBasesSRV dataBasesSRV = new DataBasesSRV();

    @Autowired
    public MatrixCtr(VisitorSrv visitorSrv, Visitor visitor) {
        this.visitorSrv = visitorSrv;
        this.visitor = visitor;
    }

    @GetMapping("/")
    public String getFirst(final HttpServletRequest request, Model model, HttpServletResponse response) {
        String userPC = ConstantsFor.getUserPC(request);
        if (request.getQueryString() != null) {
            String queryString = request.getQueryString();
            boolean pcAuth = (
                userPC.toLowerCase().contains("0:0:0:0") ||
                    userPC.contains("10.200.213") ||
                    userPC.contains("10.10.111") ||
                    userPC.contains("172.16.200"));
            if (queryString.equalsIgnoreCase("eth") && pcAuth) {
                lastLogsGetter(model);
                metricMatrixStart = System.currentTimeMillis() - metricMatrixStart;
                return "logs";
            }
        } else {
            visitorSrv.makeVisit(request);
            String userIP = userPC + ":" + request.getRemotePort() + "<-" + response.getStatus();
            model.addAttribute("yourip", userIP);
            model.addAttribute(MATRIX_STRING_NAME, new MatrixSRV());

            if (ConstantsFor.getUserPC(request).toLowerCase().contains(ConstantsFor.NO0027) ||
                ConstantsFor.getUserPC(request).toLowerCase().contains("0:0:0:0")) {
            } else {
                model.addAttribute("visit", visitor.getTimeSt() + " timestamp");
            }
        }
        metricMatrixStart = System.currentTimeMillis() - metricMatrixStart;
        return "starting";
    }

    @GetMapping ("/matrix")
    public String showResults(HttpServletRequest request, Model model) {
        visitorSrv.makeVisit(request);
        model.addAttribute(MATRIX_STRING_NAME, matrixSRV);
        model.addAttribute("workPos", matrixSRV.getWorkPos());
        model.addAttribute("headtitle", matrixSRV.getCountDB() + " позиций   " + TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - ConstantsFor.START_STAMP) + " upTime");
        metricMatrixStart = System.currentTimeMillis() - metricMatrixStart;
        return MATRIX_STRING_NAME;
    }

    @PostMapping("/matrix")
    public String getWorkPosition(@ModelAttribute(MATRIX_STRING_NAME) MatrixSRV matrixSRV, BindingResult result, Model model) {
        metricMatrixStart = System.currentTimeMillis();
        this.matrixSRV = matrixSRV;
        String workPos = this.matrixSRV.getWorkPos();
        if (!workPos.toLowerCase().contains("whois:")) {
            String workPosition = this.matrixSRV.
                getWorkPosition(
                    "select * from matrix where Doljnost like '%" + workPos + "%';");
            this.matrixSRV.setWorkPos(workPosition);
            LOGGER.info(workPosition);
            return "redirect:/matrix";
        } else {
            try {
                workPos = workPos.split(":")[1];
                String s = new WhoIsWithSRV().whoIs(workPos);
                matrixSRV.setWorkPos(s.replaceAll("\n", "<br>"));
                model.addAttribute("whois", s);
            } catch (ArrayIndexOutOfBoundsException e) {
                model.addAttribute("whois", workPos + "<p>" + e.getMessage());
                return MATRIX_STRING_NAME;
            }
            metricMatrixStart = System.currentTimeMillis() - metricMatrixStart;
            return "redirect:/matrix";
        }
    }

    Model lastLogsGetter(Model model) {
        Map<String, String> vachokEthosdistro = dataBasesSRV.getLastLogs("ru_vachok_ethosdistro");
        String logsFromDB = new TForms().fromArray(vachokEthosdistro);
        model.addAttribute("logdb", logsFromDB);
        model.addAttribute("starttime", new Date(ConstantsFor.START_STAMP));
        model.addAttribute("title", metricMatrixStart);
        return model;
    }

    @GetMapping("/git")
    public String gitOn(Model model, HttpServletRequest request) {
        visitorSrv.makeVisit(request);
        SSHFactory gitOner = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo cd /usr/home/ITDept;sudo git instaweb;exit").build();
        if (request.getQueryString() != null && request.getQueryString().equalsIgnoreCase("reboot")) {
            gitOner = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo reboot").build();
        }
        LOGGER.info(gitOner.call());
        metricMatrixStart = System.currentTimeMillis() - metricMatrixStart;
        return "redirect:http://srv-git.eatmeat.ru:1234";
    }
}