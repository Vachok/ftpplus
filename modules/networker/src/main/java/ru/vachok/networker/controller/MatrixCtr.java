package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.logic.ssh.SSHFactory;
import ru.vachok.networker.services.DataBases;
import ru.vachok.networker.services.Matrix;
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

    private static AnnotationConfigApplicationContext appCtx = IntoApplication.getAppCtx();

    private static final String MATRIX_STRING_NAME = "matrix";

    private Matrix matrix;

    private VisitorSrv visitorSrv = appCtx.getBean(VisitorSrv.class);

    private DataBases dataBases = new DataBases();

    private long metricMatrixStart = 0L;

    @GetMapping("/")
    public String getFirst(HttpServletRequest request, Model model, HttpServletResponse response) {
        Visitor visitor = visitorSrv.makeVisit(request);
        String userPC = ConstantsFor.getUserPC(request);
        if (request.getQueryString() != null) {
            String queryString = request.getQueryString();
            boolean pcAuth = (
                userPC.toLowerCase().contains("0:0:0:0") ||
                    userPC.contains("10.200.213") ||
                    userPC.contains("10.10.111"));
            if (queryString.equalsIgnoreCase("eth") && pcAuth) {
                lastLogsGetter(model);
                return "logs";
            }
        } else {
            visitorSrv.makeVisit(request);
            String userIP = userPC + ":" + request.getRemotePort() + "<-" + response.getStatus();
            model.addAttribute("yourip", userIP);
            model.addAttribute("Matrix", new Matrix());

            if (ConstantsFor.getUserPC(request).toLowerCase().contains(ConstantsFor.NO0027)) {
                model.addAttribute("visit", visitor.toString() +
                    "\nUNIQ:" + visitorSrv.uniqUsers() + "\n" +
                    visitor.getDbInfo());
            } else {
                model.addAttribute("visit", visitor.getTimeSt() + " timestamp");
            }
            return "starting";
        }
        return "starting";
    }

    private Model lastLogsGetter(Model model) {
        Map<String, String> vachokEthosdistro = dataBases.getLastLogs("ru_vachok_ethosdistro");
        String logsFromDB = new TForms().fromArray(vachokEthosdistro);
        model.addAttribute("logdb", logsFromDB);
        model.addAttribute("starttime", new Date(ConstantsFor.START_STAMP));
        model.addAttribute("title", metricMatrixStart);
        return model;
    }

    @PostMapping("/matrix")
    public String getWorkPosition(@ModelAttribute("Matrix") Matrix matrix, BindingResult result, Model model) {
        metricMatrixStart = System.currentTimeMillis();
        this.matrix = matrix;
        String workPos = this.matrix.getWorkPos();
        if (!workPos.toLowerCase().contains("whois:")) {
            String workPosition = this.matrix.
                getWorkPosition(
                    "select * from matrix where Doljnost like '%" + workPos + "%';");
            this.matrix.setWorkPos(workPosition);
            LOGGER.info(workPosition);
            return "redirect:/matrix";
        } else {
            try {
                workPos = workPos.split(":")[1];
                String s = new WhoIsWithSRV().whoIs(workPos);
                matrix.setWorkPos(s.replaceAll("\n", "<br>"));
                model.addAttribute("whois", s);
            } catch (ArrayIndexOutOfBoundsException e) {
                model.addAttribute("whois", workPos + "<p>" + e.getMessage());
                return MATRIX_STRING_NAME;
            }
            return "redirect:/matrix";
        }
    }

    @GetMapping("/matrix")
    public String showResults(HttpServletRequest request, Model model) {
        visitorSrv.makeVisit(request);
        model.addAttribute("Matrix", matrix);
        model.addAttribute("workPos", matrix.getWorkPos());
        model.addAttribute("headtitle", matrix.getCountDB() + " позиций   " + TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - ConstantsFor.START_STAMP) + " upTime");
        return MATRIX_STRING_NAME;
    }

    @GetMapping("/git")
    public String gitOn(Model model, HttpServletRequest request) {
        visitorSrv.makeVisit(request);
        SSHFactory gitOner = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo cd /usr/home/ITDept;sudo git instaweb;exit").build();
        if (request.getQueryString() != null && request.getQueryString().equalsIgnoreCase("reboot")) {
            gitOner = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo reboot").build();
        }
        LOGGER.info(gitOner.call());
        return "redirect:http://srv-git.eatmeat.ru:1234";
    }
}