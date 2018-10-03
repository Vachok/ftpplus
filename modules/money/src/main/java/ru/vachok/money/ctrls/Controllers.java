package ru.vachok.money.ctrls;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface Controllers {

    void conrolMe( HttpServletResponse response , HttpServletRequest request );

    @GetMapping
    String getMeth(Model model, HttpServletRequest request);
}
