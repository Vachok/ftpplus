package ru.vachok.money.ctrls;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.services.WhoIsWithSRV;
import ru.vachok.money.services.ParserCBRru;

import javax.servlet.http.HttpServletRequest;


/**
 * @since 20.08.2018 (22:47)
 */
@Controller
public class MoneyCtrl {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = MoneyCtrl.class.getSimpleName();
    private static final AnnotationConfigApplicationContext CTX = ConstantsFor.CONTEXT;


    @GetMapping("/money")
    public String money(Model model) {
        ParserCBRru parserCBRru = CTX.getBean(ParserCBRru.class);
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
        sb.append(CTX.getDisplayName());
        return sb.toString();
    }

    @PostMapping("/getmoney")
    public String getMoney(@ModelAttribute ParserCBRru parserCBRru, Model model, BindingResult result, HttpServletRequest request){
        WhoIsWithSRV whoIsWithSRV = CTX.getBean(WhoIsWithSRV.class);
        String s = whoIsWithSRV.whoIs( request.getRemoteAddr());
        model.addAttribute("ParserCBRru", parserCBRru);
        model.addAttribute("result", s);
        return "ok";
    }
}