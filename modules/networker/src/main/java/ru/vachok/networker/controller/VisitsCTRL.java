package ru.vachok.networker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 @since 02.10.2018 (14:18) */
@Controller
public class VisitsCTRL {


    @GetMapping("/visits")
    public String viShow(HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        return "visitors";
    }
}
