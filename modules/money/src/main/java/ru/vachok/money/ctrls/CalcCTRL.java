package ru.vachok.money.ctrls;


import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.web.beans.CalculatorForSome;


/**
 @since 09.09.2018 (15:02) */
public class CalcCTRL {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CalcCTRL.class.getSimpleName();

    /**
     {@link }
     */
    private static MessageToUser messageToUser = new MessageCons();

    private CalculatorForSome calculatorForSome;

    @PostMapping ("/count")
    public String okOk(@ModelAttribute ("CalculatorForSome") CalculatorForSome calculatorForSome, BindingResult result) {
        this.calculatorForSome = calculatorForSome;
        this.calculatorForSome.userInput = "OP";
        return "redirect:/count";
    }

    @GetMapping ("/count")
    public String resultOfCount(Model model) {
        model.addAttribute("result", calculatorForSome);
        return "calc";
    }
}