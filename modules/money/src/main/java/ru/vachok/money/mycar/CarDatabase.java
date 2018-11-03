package ru.vachok.money.mycar;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.AppFooter;
import ru.vachok.money.components.Visitor;

import javax.servlet.http.HttpServletRequest;


/**
 @since 23.08.2018 (18:40) */
@Controller
public class CarDatabase {

    private static final String CAR_DB = "car_db";

    private MyOpel myOpel;

    private Visitor visitor;

    /*Instances*/
    @Autowired
    public CarDatabase(MyOpel myOpel) {
        this.myOpel = myOpel;
        this.visitor = new Visitor();
    }

    @GetMapping("/chkcar")
    public String showEngineTMP(Model model, HttpServletRequest request) {
        visitor.setRequest(request);
        model.addAttribute("myOpel", myOpel);
        model.addAttribute("title", "Average speeds");
        String roadStat = "<center><p>" + myOpel.getAvgSpeedA107() + " A107<br>            " + myOpel.getAvgSpeedRiga() + " Novoriga</p>" +
            "<p>" + myOpel.getLastTimeA107() + " по A107. (" + myOpel.getCountA107() + " total)" + ";<br>" + myOpel.getLastTimeNRiga() + " по Риге.(" + myOpel.getCountRiga() + " total);" + "</p></center>";
        model.addAttribute("roadStat", roadStat);
        model.addAttribute("footer", new AppFooter().getTheFooter());
        return CAR_DB;
    }

    @PostMapping("/carinfo")
    public String carInfo(@ModelAttribute MyOpel myOpel) {
        this.myOpel = myOpel;
        return "redirect:/" + CAR_DB;
    }

    @GetMapping("/carinfo")
    public String defInfo(Model model) {
        model.addAttribute("title", "MAF");
        String mafAverages;
        try{
            mafAverages = myOpel.getMAFAverages(ConstantsFor.rowsCount);
        }
        catch(IndexOutOfBoundsException e){
            mafAverages = e.getLocalizedMessage();
        }
        model.addAttribute("info", mafAverages);
        model.addAttribute("footer", new AppFooter().getTheFooter());
        return CAR_DB;
    }

}