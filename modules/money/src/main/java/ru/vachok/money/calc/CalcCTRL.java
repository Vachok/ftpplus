package ru.vachok.money.calc;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.PageFooter;
import ru.vachok.money.components.Visitor;
import ru.vachok.money.config.AppComponents;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 09.09.2018 (15:02) */
@Controller
public class CalcCTRL {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CalcCTRL.class.getSimpleName();

    private CalcSrv calcSrv;

    private static final Logger LOGGER = AppComponents.getLogger();

    private CalculatorForSome calculatorForSome;

    private Visitor visitor;

    private static final String AT_NAME_DESTINY = "destiny";

    @Autowired
    private ChooseYouDestiny chooseYouDestiny;

    /*Instances*/
    @Autowired
    public CalcCTRL(CalculatorForSome calculatorForSome, CalcSrv calcSrv, Visitor visitor) {
        this.calculatorForSome = calculatorForSome;
        this.calcSrv = calcSrv;
        this.visitor =visitor;
    }


    @GetMapping ("/calc")
    public String resultOfCount(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        visitor.setSessionID(request.getSession().getId());
        visitor.setRequest(request);
        String timeStr = ConstantsFor.getAtomTime();
        model.addAttribute("CalculatorForSome", calculatorForSome);
        model.addAttribute(AT_NAME_DESTINY, chooseYouDestiny);
        calculatorForSome.setUserInput("");
        model.addAttribute(ConstantsFor.AT_NAME_TITLE, timeStr);
        model.addAttribute(ConstantsFor.AT_NAME_FOOTER, new PageFooter().getTheFooter());
        return "calc";
    }

    @PostMapping ("/calc")
    public String okOk(@ModelAttribute ("CalculatorForSome") CalculatorForSome calculatorForSome, BindingResult result, Model model) {
        this.calculatorForSome = calculatorForSome;
        String toString = calculatorForSome.toString();
        model.addAttribute(AT_NAME_DESTINY, chooseYouDestiny);
        LOGGER.info(toString);
        String userInputs = calcSrv.destinyGetter(calculatorForSome.getUserInput());
        model.addAttribute(ConstantsFor.AT_NAME_RESULT, userInputs);
        return "ok";
    }

    @PostMapping ("/destiny")
    public String destinyPOST(@ModelAttribute ChooseYouDestiny chooseYouDestiny, Model model) {
        this.chooseYouDestiny = chooseYouDestiny;
        model.addAttribute(AT_NAME_DESTINY, chooseYouDestiny);
        model.addAttribute("CalculatorForSome", calculatorForSome);
        String toString = chooseYouDestiny.toString();
        LOGGER.info(toString);
        return "calc";
    }
}