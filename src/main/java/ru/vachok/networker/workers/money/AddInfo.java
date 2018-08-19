package ru.vachok.networker.workers.money;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.pasclass.ConstantsFor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <h1>Добавит начальные данные</h1>
 *
 * @since 18.08.2018 (21:43)
 */
@Controller
public class AddInfo {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = AddInfo.class.getSimpleName();
    /**
     * {@link }
     */
    private static MessageToUser messageToUser = new MessageCons();

    private String startMoney = "0.0";


    public String getStartMoney() {
        return startMoney;
    }


    public void setStartMoney( String startMoney ) {
        this.startMoney = startMoney;
    }


    @GetMapping(value = "/money")
    public Model addMoney( Model model , HttpServletRequest httpServletRequest , HttpServletResponse httpServletResponse ) {
        model.addAttribute("usd" , ConstantsFor.USD_IN_14);
        model.addAttribute("euro" , ConstantsFor.E_IN_14);
        model.addAttribute("message" , startMoney);
        String userName = httpServletRequest.getRemoteUser();
        model.addAttribute("userName" , userName);
        return model;
    }


    @PostMapping("/takeMyMoney")
    public String showAddMoney( Model model ) {
        String babki = "takeMyMoney";
        model.addAttribute("babki" , babki);
        return babki;
    }
}