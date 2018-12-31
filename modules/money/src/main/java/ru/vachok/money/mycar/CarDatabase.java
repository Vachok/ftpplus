package ru.vachok.money.mycar;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.PageFooter;
import ru.vachok.money.components.Visitor;

import javax.servlet.http.HttpServletRequest;


/**
 @since 23.08.2018 (18:40) */
@Controller
public class CarDatabase {

    /*Fields*/
    private static final String CAR_DB = "car_db";

    private MyOpel myOpel;

    private Visitor visitor;

    /*Instances*/
    @Autowired
    public CarDatabase(MyOpel myOpel) {
        this.myOpel = myOpel;
        this.visitor = new Visitor();
    }

    @GetMapping ("/chkcar")
    public String showEngineTMP(Model model, HttpServletRequest request) {
        visitor.setRequest(request);
        model.addAttribute("myOpel", myOpel);
        model.addAttribute(ConstantsFor.TITLE, "Average speeds");
        String roadStat = "<center><p>" + myOpel.getAvgSpeedA107() + " A107<br>            " + myOpel.getAvgSpeedRiga() + " Novoriga</p>" +
            "<p>" + myOpel.getLastTimeA107() + " по A107. (" + myOpel.getCountA107() + " total)" + ";<br>" + myOpel.getLastTimeNRiga() + " по Риге.(" + myOpel.getCountRiga() + " total);" + "</p" +
            "></center>";
        model.addAttribute("roadStat", roadStat);
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getTheFooter());
        return CAR_DB;
    }

    @PostMapping ("/carinfo")
    public String carInfo(@ModelAttribute MyOpel myOpel) {
        this.myOpel = myOpel;
        return "redirect:/" + CAR_DB;
    }

    @GetMapping ("/carinfo")
    public String defInfo(Model model) {
        model.addAttribute(ConstantsFor.TITLE, "MAF");

        try{
            String mafAverages = myOpel.getMAFAverages(ConstantsFor.ROWS_COUNT);
            model.addAttribute("info", mafAverages);
        }
        catch(IndexOutOfBoundsException e){
            LoggerFactory.getLogger(this.getClass().getSimpleName());
        }

        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getTheFooter());
        return CAR_DB;
    }

}