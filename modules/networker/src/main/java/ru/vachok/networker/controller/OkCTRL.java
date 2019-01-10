package ru.vachok.networker.controller;


import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;


/**
 @since 09.11.2018 (13:37) */
@Controller
public class OkCTRL {

    @GetMapping("/ok")
    public String okStr(Model model, HttpServletRequest request) throws InvocationTargetException, NullPointerException, NoSuchBeanDefinitionException {
        Visitor visitor = AppComponents.thisVisit(request.getSession().getId());
        if (request.getQueryString() == null) throw new UnsatisfiedLinkError("Кривая ссылка!");
        else {
            String qStr = request.getQueryString();
            model.addAttribute(ConstantsFor.ATT_TITLE, qStr);
            model.addAttribute("pcs", request.getHeader("pcs"));
            AppComponents.getLogger().warn(visitor.toString());
            return "ok";
        }
    }
}
