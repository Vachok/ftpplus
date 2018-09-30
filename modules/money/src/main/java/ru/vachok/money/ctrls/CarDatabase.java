package ru.vachok.money.ctrls;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;


/**
 @since 23.08.2018 (18:40) */
@Controller
public class CarDatabase {

    @GetMapping ("/chkcar")
    public String showEngineTMP(Model model, HttpServletRequest request) {
        model.addAttribute("title");
        model.addAttribute("roadStat");
        model.addAttribute("dbStat");
        return "car_db";
    }

}