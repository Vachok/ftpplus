package ru.vachok.money.ctrls;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.components.AppFooter;
import ru.vachok.money.components.CalculatorForSome;
import ru.vachok.money.components.Visitor;
import ru.vachok.money.services.CalcSrv;

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

    private CalculatorForSome calculatorForSome;

    private Visitor visitor;

    /*Instances*/
    @Autowired
    public CalcCTRL(CalculatorForSome calculatorForSome, CalcSrv calcSrv, Visitor visitor) {
        this.calculatorForSome = calculatorForSome;
        this.calcSrv = calcSrv;
        this.visitor =visitor;
    }


    @GetMapping ("/calc")
    public String resultOfCount(Model model, HttpServletRequest request, HttpServletResponse response) {
        visitor.setSessionID(request.getSession().getId());
        visitor.setRequest(request);
        model.addAttribute("CalculatorForSome", calculatorForSome);
        calculatorForSome.setUserInput("");
        model.addAttribute("title", "CALC");
        model.addAttribute("footer", new AppFooter().getTheFooter());
        return "calc";
    }

    @PostMapping ("/calc")
    public String okOk(@ModelAttribute ("CalculatorForSome") CalculatorForSome calculatorForSome, BindingResult result, Model model) {
        this.calculatorForSome = calculatorForSome;
        String uInp = calcSrv.resultCalc(calculatorForSome.getUserInput());
        model.addAttribute("result", uInp);
        return "ok";
    }
}