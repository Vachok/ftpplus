package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vachok.networker.componentsrepo.services.AnketaKonfeta;
import ru.vachok.networker.enums.ModelAttributeNames;

import javax.servlet.http.HttpServletRequest;

/**
 Опрос юзеров
 <p>

 @since 17.01.2019 (9:44) */
@Controller
public class AnketaKonfetaCRTL {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AnketaKonfeta.class.getSimpleName());

    private static final String RET_ANKETA = "anketa";

    private AnketaKonfeta anketaKonfeta;

    @Autowired
    public AnketaKonfetaCRTL(AnketaKonfeta anketaKonfeta) {
        this.anketaKonfeta = anketaKonfeta;
    }

    @GetMapping("/anketa")
    public String getMapForAnketa(HttpServletRequest request, Model model) {
        anketaKonfeta.setAllAsEmptyString();
    
        if (request.getQueryString() != null) {
            LOGGER.warn(request.getQueryString());
        }
        model.addAttribute(ModelAttributeNames.TITLE, this.getClass().getSimpleName());
        model.addAttribute("anketaKonfeta", anketaKonfeta);
        model.addAttribute("anketahead", "IT-опросник");
        return RET_ANKETA;
    }

    @PostMapping("/anketaok")
    public String postAnketa(@ModelAttribute AnketaKonfeta anketaKonfeta, Model model, HttpServletRequest request) {
        this.anketaKonfeta = anketaKonfeta;
        anketaKonfeta.setUserIp(request.getRemoteAddr());
        model.addAttribute("anketaKonfeta", anketaKonfeta);
        model.addAttribute("ok", "СПАСИБО!");
        LOGGER.warn(request.getQueryString());
        return "ok";
    }
}
