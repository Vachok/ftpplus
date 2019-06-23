package ru.vachok.networker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.services.SimpleCalculator;

import javax.servlet.http.HttpServletRequest;

/**
 Контроллер для /calculate

 @since 15.11.2018 (12:47) */
@Controller("calculate")
public class CalculateCTRL {

    private SimpleCalculator simpleCalculator;

    @Autowired
    public CalculateCTRL(SimpleCalculator simpleCalculator) {
        this.simpleCalculator = simpleCalculator;
    }

    @GetMapping("/calculate")
    public String getM(Model model, HttpServletRequest request) {
        model.addAttribute(ConstantsFor.ATT_TITLE, "Calculator");
        model.addAttribute(ConstantsFor.BEANNAME_CALCULATOR, simpleCalculator);
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<p>");
        if (request != null & request.getQueryString() != null) {
            model.addAttribute(ConstantsFor.ATT_RESULT, simpleCalculator.getStampFromDate(request.getQueryString()));
        }
        return "calculate";
    }

    @PostMapping("/calculate")
    public String timeStamp(@ModelAttribute SimpleCalculator simpleCalculator, Model model, String workPos) {
        model.addAttribute(ConstantsFor.ATT_TITLE, "Calculator-POS");
        model.addAttribute(ConstantsFor.BEANNAME_CALCULATOR, simpleCalculator);
        model.addAttribute(ConstantsFor.ATT_RESULT, simpleCalculator.getStampFromDate(workPos));
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "calculate";
    }

}
