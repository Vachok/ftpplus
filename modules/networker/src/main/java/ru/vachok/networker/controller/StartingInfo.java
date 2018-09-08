package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.beans.DataBases;
import ru.vachok.networker.beans.FileMessenger;
import ru.vachok.networker.beans.Matrix;
import ru.vachok.networker.config.AppComponents;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @since 07.09.2018 (0:35)
 */
@Controller
public class StartingInfo {

    /*Fields*/

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = StartingInfo.class.getSimpleName();

    private static final Logger LOGGER = AppComponents.getLogger();


    /**
     * {@link }
     */
    private static MessageToUser messageToUser = new FileMessenger();

    private Matrix matrix;

    private long metricMatrixStart = 0L;

    @GetMapping("/")
    public String getFirst(HttpServletRequest request, Model model, HttpServletResponse response) {
        if (request.getQueryString() != null) {
            String queryString = request.getQueryString();
            if(queryString.equalsIgnoreCase("eth")){
                model = lastLogsGetter(model);
                return "logs";
            }

        } else {
            String userIP = ConstantsFor.getPC(request) + ":" + request.getRemotePort() + "<-" + response.getStatus();
            model.addAttribute("yourip", userIP);
            model.addAttribute("Matrix", new Matrix());
            return "starting";
        }
        return "starting";
    }

    private Model lastLogsGetter(Model model) {
        Map<String, String> ru_vachok_ethosdistro = new DataBases().getLastLogs("ru_vachok_ethosdistro");
        String logsFromDB = new TForms().fromArray(ru_vachok_ethosdistro);
        model.addAttribute("logdb", logsFromDB);
        model.addAttribute("starttime", new Date(ConstantsFor.START_STAMP));
        model.addAttribute("date", metricMatrixStart);
        return model;
    }

    @PostMapping("/matrix")
    public String getWorkPosition(@ModelAttribute("Matrix") Matrix matrix, BindingResult result) {
        metricMatrixStart = System.currentTimeMillis();
        this.matrix = matrix;
        LOGGER.info(this.matrix.getWorkPos());
        String workPosition = this.matrix.
            getWorkPosition(
                "select * from matrix where Doljnost like '%" + this.matrix.getWorkPos() + "%';");
        this.matrix.setWorkPos(workPosition);
        LOGGER.info(workPosition);
        return "ok";
    }

    @GetMapping ("/matrix")
    public String showResults(HttpServletRequest request, Model model) {
        model.addAttribute("Matrix", matrix);
        model.addAttribute("workPos", matrix.getWorkPos());
        model.addAttribute("headtitle", matrix.getCountDB() + " позиций   " + TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - ConstantsFor.START_STAMP) + " upTime");
        return "matrix";
    }
}