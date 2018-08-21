package ru.vachok.money.ctrls;



import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;


/**
 * @since 20.08.2018 (22:56)
 */
@Controller
public class ErrCtrl implements ErrorController {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ErrCtrl.class.getSimpleName();
    private String errStr;


    @ResponseBody
    public static String stackErr( Exception e ) {
        ErrCtrl errCtrl = new ErrCtrl();
        return errCtrl.err(e);
    }


    /**
     * Путь к ошибке
     *
     * @return /err
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }


    @RequestMapping("/error")
    @ResponseBody
    public String err(Exception e) {
       errStr = e.getMessage()+"<p>"+ Arrays.toString(e.getStackTrace()).replaceAll(", ", "<p>");
       return errStr + new Date().toString() +"<p> <h1>ERROR</h1><p>";
    }
}