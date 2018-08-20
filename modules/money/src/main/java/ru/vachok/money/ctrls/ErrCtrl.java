package ru.vachok.money.ctrls;



import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
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


    public static void stackErr( Exception e ) {
        ErrCtrl errCtrl = new ErrCtrl();
        errCtrl.errStr = e.getMessage() + " \n" + e.getStackTrace();
        errCtrl.getErrorPath();
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
    public String err( HttpServletRequest request ) {
        return errStr + new Date().toString() + " \n" + request.getQueryString();
    }
}