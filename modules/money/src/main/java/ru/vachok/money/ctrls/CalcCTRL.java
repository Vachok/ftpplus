package ru.vachok.money.ctrls;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.CalculatorForSome;
import ru.vachok.money.services.CalcSrv;


/**
 @since 09.09.2018 (15:02) */
@Controller
public class CalcCTRL {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CalcCTRL.class.getSimpleName();

    /**
     {@link }
     */
    private static final AnnotationConfigApplicationContext CTX = ConstantsFor.CONTEXT;

    private CalcSrv calcSrv = CTX.getBean(CalcSrv.class);
    private CalculatorForSome calculatorForSome = CTX.getBean(CalculatorForSome.class);

    private String s;

    @PostMapping ("/calc")
    public String okOk(@ModelAttribute ("CalculatorForSome") CalculatorForSome calculatorForSome, BindingResult result, Model model) {
        this.calculatorForSome = calculatorForSome;
        this.s = calcSrv.resultCalc(); //fixme 19.09.2018 (22:08)
        model.addAttribute("result", s);
        return "calc";
    }

    @GetMapping ("/calc")
    public String resultOfCount(Model model) {
        model.addAttribute("CalculatorForSome", calculatorForSome);
        model.addAttribute("title", "CALC");
        model.addAttribute("inp", calculatorForSome.toString());
        return "calc";
    }
}