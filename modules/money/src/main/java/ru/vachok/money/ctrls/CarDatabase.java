package ru.vachok.money.ctrls;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vachok.money.components.MyOpel;


/**
 @since 23.08.2018 (18:40) */
@Controller
public class CarDatabase {

    @GetMapping ("/chkcar")
    public String showEngineTMP(Model model, @ModelAttribute MyOpel opel) {
        model.addAttribute("opel", opel);
        return "car_db";
    }

}