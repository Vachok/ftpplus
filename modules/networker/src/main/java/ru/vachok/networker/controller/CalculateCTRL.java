// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.componentsrepo.services.SimpleCalculator;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;

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
        model.addAttribute(ModelAttributeNames.TITLE, "Calculator");
        model.addAttribute(ConstantsFor.BEANNAME_CALCULATOR, simpleCalculator);
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER) + "<p>");
        if (request != null & request.getQueryString() != null) {
            model.addAttribute(ModelAttributeNames.ATT_RESULT, simpleCalculator.getStampFromDate(request.getQueryString()));
        }
        return "calculate";
    }
    
    @PostMapping("/calculate")
    public String timeStamp(@ModelAttribute SimpleCalculator simpleCalculator, @NotNull Model model, String workPos) {
        model.addAttribute(ModelAttributeNames.TITLE, "Calculator-POS");
        model.addAttribute(ConstantsFor.BEANNAME_CALCULATOR, simpleCalculator);
        model.addAttribute(ModelAttributeNames.ATT_RESULT, simpleCalculator.getStampFromDate(workPos));
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER));
        return "calculate";
    }
    
}
