package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.components.CalculatorForSome;
import ru.vachok.money.components.Visitor;
import ru.vachok.money.services.CalcSrv;
import ru.vachok.money.services.CookieMaker;

import javax.servlet.http.Cookie;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SOURCE_CLASS);

    private CalcSrv calcSrv;

    private CalculatorForSome calculatorForSome;

    private Visitor visitor;

    private CookieMaker cookieMaker;

    /*Instances*/
    @Autowired
    public CalcCTRL(CalculatorForSome calculatorForSome, CalcSrv calcSrv) {
        this.calculatorForSome = calculatorForSome;
        this.calcSrv = calcSrv;
    }


    @GetMapping ("/calc")
    public String resultOfCount(Model model, HttpServletRequest request, HttpServletResponse response) {
        cook(request, response);
        model.addAttribute("CalculatorForSome", calculatorForSome);
        model.addAttribute("title", "CALC");
        return "calc";
    }

    private void cook(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if(cookies.length > 0){
            for(Cookie cookie : cookies){
                String s = cookie.getName() + "\n" +
                    cookie.getValue() + " value\n" +
                    cookie.getComment() + " comment\n" +
                    cookie.getDomain();
                LOGGER.warn(s);
            }
        }
        else{
            response.addCookie(cookieMaker.startSession(request.getSession().getId()));
        }
    }

    @PostMapping ("/calc")
    public String okOk(@ModelAttribute ("CalculatorForSome") CalculatorForSome calculatorForSome, BindingResult result, Model model) {
        this.calculatorForSome = calculatorForSome;
        String uInp = calcSrv.resultCalc(calculatorForSome.getUserInput());
        model.addAttribute("result", uInp);
        return "ok";
    }

}