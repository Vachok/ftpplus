package ru.vachok.money.ctrls;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.components.ParserCBRruSRV;
import ru.vachok.money.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;


/**
 * @since 20.08.2018 (22:47)
 */
@Controller
public class MoneyCtrl {

    private ParserCBRruSRV parserCBRruSRV;

    private WhoIsWithSRV whoIsWithSRV;

    /*Instances*/
    @Autowired
    public MoneyCtrl(ParserCBRruSRV parserCBRruSRV, WhoIsWithSRV whoIsWithSRV) {
    }

    @GetMapping("/money")
    public String money(Model model) {
        model.addAttribute("ParserCBRruSRV", parserCBRruSRV);
        model.addAttribute("currency", "in progress...");
        model.addAttribute("title", parserCBRruSRV.getUserInput());
        return "money";
    }

    @PostMapping("/getmoney")
    public String getMoney(@ModelAttribute ParserCBRruSRV parserCBRruSRV, Model model, BindingResult result, HttpServletRequest request) {
        String s = whoIsWithSRV.whoIs( request.getRemoteAddr());
        model.addAttribute("ParserCBRruSRV", parserCBRruSRV);
        model.addAttribute("result", s);
        return "ok";
    }
}