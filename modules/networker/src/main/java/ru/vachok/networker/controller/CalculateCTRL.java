// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.info.HTMLGeneration;
import ru.vachok.networker.info.PageGenerationHelper;
import ru.vachok.networker.services.SimpleCalculator;

import javax.servlet.http.HttpServletRequest;


/**
 Контроллер для /calculate
 
 @since 15.11.2018 (12:47) */
@Controller("calculate")
public class CalculateCTRL {
    
    
    private final HTMLGeneration pageFooter = new PageGenerationHelper();
    
    private SimpleCalculator simpleCalculator;
    
    @Autowired
    public CalculateCTRL(SimpleCalculator simpleCalculator) {
        this.simpleCalculator = simpleCalculator;
    }
    
    @GetMapping("/calculate")
    public String getM(@NotNull Model model, @NotNull HttpServletRequest request) {
        model.addAttribute(ModelAttributeNames.ATT_TITLE, "Calculator");
        model.addAttribute(ConstantsFor.BEANNAME_CALCULATOR, simpleCalculator);
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getFooter(ModelAttributeNames.ATT_FOOTER) + "<p>");
        if (request != null & request.getQueryString() != null) {
            model.addAttribute(ModelAttributeNames.ATT_RESULT, simpleCalculator.getStampFromDate(request.getQueryString()));
        }
        return "calculate";
    }
    
    @PostMapping("/calculate")
    public String timeStamp(@ModelAttribute SimpleCalculator simpleCalculator, @NotNull Model model, String workPos) {
        model.addAttribute(ModelAttributeNames.ATT_TITLE, "Calculator-POS");
        model.addAttribute(ConstantsFor.BEANNAME_CALCULATOR, simpleCalculator);
        model.addAttribute(ModelAttributeNames.ATT_RESULT, simpleCalculator.getStampFromDate(workPos));
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getFooter(ModelAttributeNames.ATT_FOOTER));
        return "calculate";
    }
    
}
