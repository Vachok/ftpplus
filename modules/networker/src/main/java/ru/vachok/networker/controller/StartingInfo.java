package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
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


/**
 * @since 07.09.2018 (0:35)
 */
@Controller
@SessionAttributes
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

    @GetMapping("/")
    public String getFirst(HttpServletRequest request, Model model, HttpServletResponse response) {
        if (request.getQueryString() != null) {
            String queryString = request.getQueryString();
            if (queryString.equalsIgnoreCase("eth")) model = lastLogsGetter(model);
            return "starting";
        } else {
            String userIP = ConstantsFor.getPC(request) + ":" + request.getRemotePort() + "<-" + response.getStatus();
            model.addAttribute("yourip", userIP);
            model.addAttribute("Matrix", new Matrix());
            return "starting";
        }
    }

    private Model lastLogsGetter(Model model) {
        Map<String, String> ru_vachok_ethosdistro = new DataBases().getLastLogs("ru_vachok_ethosdistro");
        model.addAttribute("logdb", new TForms().fromArray(ru_vachok_ethosdistro));
        model.addAttribute("starttime", new Date(ConstantsFor.START_STAMP));
        return model;
    }

    @PostMapping("/matrix")
    public String getWorkPosition(@ModelAttribute("Matrix") Matrix matrix, BindingResult result) {
        LOGGER.info(matrix.getWorkPos());
        String workPosition = matrix.getWorkPosition("select * from 'internet' where 'Doljnost' is like '%" + result.getTarget() + "%';");
        return workPosition;
    }

}