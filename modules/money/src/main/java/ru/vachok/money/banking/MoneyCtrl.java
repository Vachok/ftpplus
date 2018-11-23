package ru.vachok.money.banking;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.components.AppFooter;
import ru.vachok.money.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;


/**
 * @since 20.08.2018 (22:47)
 */
@Controller
public class MoneyCtrl {

    private WhoIsWithSRV whoIsWithSRV;

    private ParserCBRruSRV parserCBRruSRV;

    /*Instances*/
    @Autowired
    public MoneyCtrl(WhoIsWithSRV whoIsWithSRV, ParserCBRruSRV parserCBRruSRV) {
        this.whoIsWithSRV = whoIsWithSRV;
        this.parserCBRruSRV = parserCBRruSRV;
    }

    @GetMapping("/money")
    public String money(Model model) {
        model.addAttribute("ParserCBRruSRV", parserCBRruSRV);
        model.addAttribute("currency", parserCBRruSRV.usdCur());
        model.addAttribute("footer", new AppFooter().getTheFooter());
        return "money";
    }

    @PostMapping("/getmoney")
    public String getMoney(@ModelAttribute ParserCBRruSRV parserCBRruSRV, Model model, BindingResult result, HttpServletRequest request) {
        String s = whoIsWithSRV.whoIs( request.getRemoteAddr());
        model.addAttribute("ParserCBRruSRV", parserCBRruSRV);
        model.addAttribute("title", parserCBRruSRV.getUserInput());
        model.addAttribute("result", s);
        return "ok";
    }
}