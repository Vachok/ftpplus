package ru.vachok.networker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.beans.DataBases;
import ru.vachok.networker.beans.FileMessenger;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;


/**
 @since 07.09.2018 (0:35) */
@Controller
public class StartingInfo {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = StartingInfo.class.getSimpleName();

    /**
     {@link }
     */
    private static MessageToUser messageToUser = new FileMessenger();

    @GetMapping ("/")
    public String getFirst(HttpServletRequest request, Model model) {
        Map<String, String> ru_vachok_ethosdistro = new DataBases().getLastLogs("ru_vachok_ethosdistro");
        model.addAttribute("logdb", new TForms().fromArray(ru_vachok_ethosdistro));
        model.addAttribute("starttime", new Date(ConstantsFor.START_STAMP));
        return "starting";
    }
}