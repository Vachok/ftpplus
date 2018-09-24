package ru.vachok.networker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.logic.PassGenerator;

import javax.servlet.http.HttpServletRequest;


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
    @GetMapping("/gen")
    public String passGen(HttpServletRequest request, Model model) {
        PassGenerator passGenerator = new PassGenerator();
        int howMuchBytes = 17;
        if (request.getQueryString() != null) {
            try {
                howMuchBytes = Integer.parseInt(request.getQueryString());
                model.addAttribute("pass", passGenerator.generatorPass(howMuchBytes));
            } catch (Exception e) {
                model.addAttribute("pass", e.getMessage());
                return "ad";
            }
        }
        model.addAttribute("title", howMuchBytes);
        model.addAttribute("pass", passGenerator.generatorPass(howMuchBytes));
        return "ad";
    }
}
