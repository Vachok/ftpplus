package ru.vachok.money.ctrls;



import org.slf4j.Logger;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.ApplicationConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;


/**
 * @since 20.08.2018 (22:56)
 */
@Controller
public class ErrCtrl implements ErrorController {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ErrCtrl.class.getSimpleName();
    private static Logger logger = new ApplicationConfiguration().getLogger();
    private String errStr;


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
        this.errStr = "<p>" + Arrays.toString(e.getStackTrace()).replaceAll(", " , "<br>") + "</p>";
        String eM = e.getMessage();
        model.addAttribute("error" , errStr);
        model.addAttribute("eMessage" , eM);
        return "error";
    }


    @GetMapping("/exit")
    public void exitApp( HttpServletRequest httpServletRequest , HttpServletResponse response ) throws IOException {
        String s = httpServletRequest.getRequestURL().toString();
        String q = httpServletRequest.getQueryString();
        logger.info(String.format("%s%n%s" , s , q));
        if (q != null) {
            if (q.contains("shutdown")) Runtime.getRuntime().exec("shutdown /p /f");
            if (q.contains("restart")) Runtime.getRuntime().exec("shutdown /r /f");
        } else {
            response.sendRedirect("http://10.10.111.57/e");
            System.exit(0);
        }
    }
}