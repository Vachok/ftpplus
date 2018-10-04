package ru.vachok.money.ctrls;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.components.AppFooter;
import ru.vachok.money.components.MyOpel;

import javax.servlet.http.HttpServletRequest;


/**
 @since 23.08.2018 (18:40) */
@Controller
public class CarDatabase {

    private final MyOpel myOpel;

    /*Instances*/
    @Autowired
    public CarDatabase(MyOpel myOpel) {
        this.myOpel = myOpel;
    }


    @GetMapping ("/chkcar")
    public String showEngineTMP(Model model, HttpServletRequest request) {
        model.addAttribute("title", myOpel.getCarName());
        String roadStat = "<center><p>" + myOpel.getAvgSpeedA107() + " A107<br>            " + myOpel.getAvgSpeedRiga() + " Novoriga</p>" +
            "<p>" + myOpel.getLastTimeA107() + " по A107. ("+myOpel.getCountA107()+" total)"+";<br>" + myOpel.getLastTimeNRiga() + " по Риге.("+myOpel.getCountRiga()+" total);"+"</p></center>";
        model.addAttribute("roadStat", roadStat);
        model.addAttribute("footer", new AppFooter().getTheFooter());
        return "car_db";
    }

}