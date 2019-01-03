package ru.vachok.money.banking;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.PageFooter;


/**
 @since 20.08.2018 (22:47) */
@Controller
public class MoneyCtrl {

    private ParserCBRruSRV parserCBRruSRV;

    private Currencies currencies;

    @Autowired
    public MoneyCtrl(ParserCBRruSRV parserCBRruSRV, Currencies currencies) {
        this.currencies = currencies;
        this.parserCBRruSRV = parserCBRruSRV;
    }

    @GetMapping("/money")
    public String money(Model model) {
        parserCBRruSRV.curDownloader();
        model.addAttribute(ConstantsFor.PARSER_CB_RRU_SRV, parserCBRruSRV);
        model.addAttribute(ConstantsFor.CURRENCY, parserCBRruSRV.countYourMoney());
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getTheFooter());
        return "money";
    }

    @PostMapping("/getmoney")
    public String getMoney(@ModelAttribute ParserCBRruSRV parserCBRruSRV, Model model, @ModelAttribute Currencies currencies) {
        this.currencies = currencies;
        model.addAttribute(ConstantsFor.PARSER_CB_RRU_SRV, parserCBRruSRV);
        model.addAttribute(ConstantsFor.TITLE, parserCBRruSRV.getUserMoney());
        model.addAttribute(ConstantsFor.RESULT, currencies.toString() + "<p>" + parserCBRruSRV.toString());
        return "ok";
    }
}