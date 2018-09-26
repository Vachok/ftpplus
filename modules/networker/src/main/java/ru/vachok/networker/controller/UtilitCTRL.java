package ru.vachok.networker.controller;


import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.componentsrepo.ADComputer;
import ru.vachok.networker.componentsrepo.ADUser;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.logic.PassGenerator;
import ru.vachok.networker.services.ADSrv;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 <b>Контроллер полезных утилит</b>
 {@link #passGen(HttpServletRequest, Model)}

 @since 22.08.2018 (10:17) */
@Controller
public class UtilitCTRL {

    private AutowireCapableBeanFactory autowireCapableBeanFactory = new AppCtx().getAutowireCapableBeanFactory();

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

    @GetMapping("/ad")
    public String adUsersComps(HttpServletRequest request, Model model) {
        ADSrv adSrv = AppComponents.adSrv();
        adSrv.run();
        ADComputer adComputer = adSrv.getAdComputer();
        ADUser adUser = adSrv.getAdUser();
        new AppCtx().getAutowireCapableBeanFactory();
        List<ADComputer> adComputers = adComputer.getAdComputers();
        List<ADUser> adUsers = adUser.getAdUsers();
        StringBuilder stringBuilder = new StringBuilder();
        adComputers.forEach((x -> stringBuilder.append(x.toString())));
        stringBuilder.append("<br>");
        model.addAttribute("pcs", stringBuilder.toString());
        return "ok";
    }
}
