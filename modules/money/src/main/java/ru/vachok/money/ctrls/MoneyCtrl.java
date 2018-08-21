package ru.vachok.money.ctrls;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.logic.ParseCurrency;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @since 20.08.2018 (22:47)
 */
@Controller
public class MoneyCtrl {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = MoneyCtrl.class.getSimpleName();


    @GetMapping("/money")
    public String money(@RequestParam (value = "currency", required = false, defaultValue="") String currency, Model model) {
        currency = new ParseCurrency().getTodayUSD();
        model.addAttribute("currency", currency);
        return "money";
    }



    public String exitApp() {
        System.exit(0);
        return "redirect:http://10.10.111.57:8880/exit";

    }
}