package ru.vachok.money.ctrls;


import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.PageFooter;
import ru.vachok.money.filesys.FileSysWorker;
import ru.vachok.money.services.TForms;
import ru.vachok.money.services.TimeChecker;

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
        FileSysWorker.writeFile("motd", new TForms().toStringFromArray(e, false));
        model.addAttribute(ConstantsFor.ERROR, new TForms().toStringFromArray(e, true));
        model.addAttribute("errmessage", eM);
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getTheFooter());
        return ConstantsFor.ERROR;
    }


    @GetMapping("/exit")
    public void exitApp( HttpServletRequest httpServletRequest , HttpServletResponse response ) throws IOException {
        String s = httpServletRequest.getRequestURL().toString();
        String q = httpServletRequest.getQueryString();
        FileSysWorker.writeFile("motd", s + "\n" + q + "\n" + new TimeChecker().toString());
        if (q != null) {
            if(q.contains("shutdown")){
                Runtime.getRuntime().exec(ConstantsFor.SHUTDOWN_P_F);
            }
            if(q.contains("restart")){
                Runtime.getRuntime().exec(ConstantsFor.SHUTDOWN_R_F);
            }
        } else {
            System.exit(0);
        }
    }
}