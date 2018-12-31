package ru.vachok.money.ctrls;


import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.PageFooter;
import ru.vachok.money.services.TForms;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @since 20.08.2018 (22:56)
 */
@Controller
public class ErrCtrl implements ErrorController {

    /**
     * Путь к ошибке
     *
     * @return /error
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }


    @GetMapping("/error")
    public String err( Exception e , Model model ) {
        String eM = e.getMessage();
        model.addAttribute("error", new TForms().toStringFromArray(e, true));
        model.addAttribute("errmessage", eM);
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getTheFooter());
        return "error";
    }


    @GetMapping("/exit")
    public void exitApp( HttpServletRequest httpServletRequest , HttpServletResponse response ) throws IOException {
        String s = httpServletRequest.getRequestURL().toString();
        String q = httpServletRequest.getQueryString();
        if (q != null) {
            if (q.contains("shutdown")) Runtime.getRuntime().exec("shutdown /p /f");
            if (q.contains("restart")) Runtime.getRuntime().exec("shutdown /r /f");
        } else {
            System.exit(0);
        }
    }
}