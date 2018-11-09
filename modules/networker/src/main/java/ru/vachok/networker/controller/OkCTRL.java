package ru.vachok.networker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.componentsrepo.AppComponents;

import javax.servlet.http.HttpServletRequest;

/**
 @since 09.11.2018 (13:37) */
@Controller
public class OkCTRL {

    @GetMapping("/ok")
    public String okStr(Model model, HttpServletRequest request) {
        if (request.getQueryString() == null) throw new UnsatisfiedLinkError("Кривая ссылка!");
        else {
            String qStr = request.getQueryString();
            model.addAttribute("title", qStr);
            model.addAttribute("pcs", request.getHeader("pcs"));
            AppComponents.getLogger().warn("OK!");
            return "ok";
        }
    }
}
