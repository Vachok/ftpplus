package ru.vachok.networker.controller;


import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.services.PassGenerator;
import ru.vachok.networker.services.SimpleCalculator;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 <b>Контроллер полезных утилит</b>
 {@link #passGen(HttpServletRequest, Model)}

 @since 22.08.2018 (10:17) */
@Controller
public class UtilitCTRL {

    /**
     <b>Генератор случайной последовательности. ("/gen")</b>
     Генерирует последовательность байтов. По-умолчанию 17 байт.<br> Можно использовать {@link HttpServletRequest#getQueryString()}, тогда кол-во байт = числу после ?

     @param request {@link HttpServletRequest}
     @param model   {@link Model}
     @return "ad".html
     */
    @GetMapping ("/gen")
    private String passGen(HttpServletRequest request, Model model) {
        Thread.currentThread().setName("UtilitCTRL.passGen");
        PassGenerator passGenerator = new PassGenerator();
        int howMuchBytes = 17;
        if(request.getQueryString()!=null){
            try{
                howMuchBytes = Integer.parseInt(request.getQueryString());
                model.addAttribute("ok", passGenerator.generatorPass(howMuchBytes));
            }
            catch(Exception e){
                model.addAttribute("ok", e.getMessage());
                return "ok";
            }
        }
        model.addAttribute(ConstantsFor.ATT_TITLE, howMuchBytes);
        model.addAttribute("ok", passGenerator.generatorPass(howMuchBytes));
        return "ok";
    }

    public double getSumm(List<Double> forCountList) {
        SimpleCalculator simpleCalculator = new SimpleCalculator();
        double v = simpleCalculator.countDoubles(forCountList);
        String msg = v + " = summ";
        LoggerFactory.getLogger(UtilitCTRL.class.getName()).info(msg);
        return v;
    }

}
