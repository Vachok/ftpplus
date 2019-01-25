package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.services.AnketaKonfeta;

import javax.servlet.http.HttpServletRequest;

/**
 Опрос юзеров
 <p>

 @since 17.01.2019 (9:44) */
@Controller
public class AnketaKonfetaCRTL {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String RET_ANKETA = "anketa";

    private AnketaKonfeta anketaKonfeta;

    @Autowired
    public AnketaKonfetaCRTL(AnketaKonfeta anketaKonfeta) {
        this.anketaKonfeta = anketaKonfeta;
        Thread.currentThread().setName("AnketaKonfetaCRTL.AnketaKonfetaCRTL");
    }

    @GetMapping("/anketa")
    public String getMapForAnketa(HttpServletRequest request, Model model) {
        anketaKonfeta.setAll();
        Visitor visitor = ConstantsFor.getVis(request);
        LOGGER.info(visitor.toString());
        if (request.getQueryString() != null) {
            LOGGER.warn(request.getQueryString());
        }
        model.addAttribute(ConstantsFor.ATT_TITLE, this.getClass().getSimpleName());
        model.addAttribute("anketaKonfeta", anketaKonfeta);
        model.addAttribute("anketahead", "IT-опросник");
        return RET_ANKETA;
    }

    @PostMapping("/anketaok")
    public String postAnketa(@ModelAttribute AnketaKonfeta anketaKonfeta, Model model, HttpServletRequest request) {
        Visitor visitor = ConstantsFor.getVis(request);
        this.anketaKonfeta = anketaKonfeta;
        model.addAttribute("anketaKonfeta", anketaKonfeta);
        model.addAttribute("ok", "СПАСИБО!");
        LOGGER.warn(request.getQueryString());
        anketaKonfeta.sendKonfeta(visitor.toString());
        return "ok";
    }
}
