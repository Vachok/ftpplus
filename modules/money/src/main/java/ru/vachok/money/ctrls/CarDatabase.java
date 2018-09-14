package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vachok.money.components.MyOpel;
import ru.vachok.money.config.AppComponents;


/**
 @since 23.08.2018 (18:40) */
@Controller
public class CarDatabase {

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = CarDatabase.class.getSimpleName();

    private static final Logger LOGGER = AppComponents.getLogger();
    @GetMapping ("/chkcar")
    public String showEngineTMP(Model model, @ModelAttribute MyOpel opel) {
        model.addAttribute("opel", opel);
        return "car_db";
    }

}