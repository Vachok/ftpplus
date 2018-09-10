package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vachok.money.ApplicationConfiguration;
import ru.vachok.money.logic.TForms;
import ru.vachok.money.web.beans.MyOpel;


/**
 @since 23.08.2018 (18:40) */
@Controller
public class CarDatabase {

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CarDatabase.class.getSimpleName();

    private static final Logger LOGGER = ApplicationConfiguration.getLogger();
    @GetMapping ("/chkcar")
    public String showEngineTMP(Model model, @ModelAttribute MyOpel opel) {
        LOGGER.info(opel.getSpeedStr());
        model.addAttribute("opel", opel);
        model.addAttribute("roadStat", new TForms().toStringFromArray(opel.getObdDataMap()));
        model.addAttribute("dbStat", opel.getSpeedStr());
        return "car_db";
    }

}