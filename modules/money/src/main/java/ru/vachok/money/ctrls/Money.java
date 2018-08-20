package ru.vachok.money.ctrls;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @since 20.08.2018 (22:47)
 */
@Controller
public class Money {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = Money.class.getSimpleName();


    @GetMapping("/")
    @ResponseBody
    public String money( HttpServletRequest request , HttpServletResponse response ) {
        String s = request.getRequestURL().toString() + "\n" + response.getStatus() + " req STATUS";
        return s + "<a href='http://10.10.111.57:8881/e'>EXIT</a>";
    }


    @GetMapping("/e")
    public String exitApp() {
        System.exit(0);
        return "redirect:http://10.10.111.57:8880/exit";

    }
}