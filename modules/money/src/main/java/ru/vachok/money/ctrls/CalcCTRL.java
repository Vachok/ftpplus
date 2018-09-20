package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.CalculatorForSome;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.services.CalcSrv;
import ru.vachok.money.services.CookieMaker;
import ru.vachok.money.services.VisitorSrv;

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

    /**
     {@link }
     */
    private static final AnnotationConfigApplicationContext CTX = ConstantsFor.CONTEXT;

    private CalcSrv calcSrv = CTX.getBean(CalcSrv.class);
    private CalculatorForSome calculatorForSome = CTX.getBean(CalculatorForSome.class);

    private static final Logger LOGGER = AppComponents.getLogger();


    @GetMapping ("/calc")
    public String resultOfCount(Model model, HttpServletRequest request, HttpServletResponse response) {
        cook(request, response);
        CTX.getBean(VisitorSrv.class).makeVisit(request, response);
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
            CookieMaker cookieMaker = CTX.getBean(CookieMaker.class);
            response.addCookie(cookieMaker.startSession(request.getSession().getId()));
        }
    }

    @PostMapping ("/calc")
    public String okOk(@ModelAttribute ("CalculatorForSome") CalculatorForSome calculatorForSome, BindingResult result, Model model) {
        this.calculatorForSome = calculatorForSome;
        model.addAttribute("result", calcSrv.resultCalc(calculatorForSome.getUserInput()));
        model.addAttribute("title", CTX.getApplicationName());
        return "ok";
    }

}