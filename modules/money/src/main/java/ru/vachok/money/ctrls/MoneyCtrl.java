package ru.vachok.money.ctrls;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.components.ParserCBRru;
import ru.vachok.money.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;


/**
 * @since 20.08.2018 (22:47)
 */
@Controller
public class MoneyCtrl {

    private ParserCBRru parserCBRru;

    private WhoIsWithSRV whoIsWithSRV;

    /*Instances*/
    @Autowired
    public MoneyCtrl(ParserCBRru parserCBRru, WhoIsWithSRV whoIsWithSRV) {
    }

    @GetMapping("/money")
    public String money(Model model) {
        model.addAttribute("ParserCBRru", parserCBRru);
        model.addAttribute("map", oldInfoCurGet());
        model.addAttribute("currency", "in progress...");
        model.addAttribute("title", parserCBRru.getUserInput());
        return "money";
    }

    private String oldInfoCurGet() {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>").append(ParserCBRru.getWelcomeNewUser());
        sb.append("</p>");
        return sb.toString();
    }

    @PostMapping("/getmoney")
    public String getMoney(@ModelAttribute ParserCBRru parserCBRru, Model model, BindingResult result, HttpServletRequest request){
        String s = whoIsWithSRV.whoIs( request.getRemoteAddr());
        model.addAttribute("ParserCBRru", parserCBRru);
        model.addAttribute("result", s);
        return "ok";
    }
}