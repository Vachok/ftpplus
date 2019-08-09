package ru.vachok.networker.controller;


import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.enums.ModelAttributeNames;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;


/**
 @see ru.vachok.networker.controller.OkCTRLTest
 @since 09.11.2018 (13:37) */
@Controller
public class OkCTRL {

    @GetMapping("/ok")
    public String okStr(Model model, HttpServletRequest request) throws InvocationTargetException, NullPointerException, NoSuchBeanDefinitionException {
        Visitor visitor = UsefulUtilities.getVis(request);
        if (request.getQueryString() == null) throw new UnsatisfiedLinkError("Кривая ссылка!");
        else {
            String qStr = request.getQueryString();
            model.addAttribute(ModelAttributeNames.ATT_TITLE, qStr);
            model.addAttribute("pcs", request.getHeader("pcs") + "<p>" + visitor);
            return "ok";
        }
    }
}
