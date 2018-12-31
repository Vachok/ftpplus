package ru.vachok.money.mapnav;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.PageFooter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 @since 29.10.2018 (13:06) */
@Controller
public class MapCTRL {

    private MapperUnit mapperUnit = new MapperUnit();

    @GetMapping("/nav")
    public String navMod(Model model, HttpServletRequest request, HttpServletResponse response) {
        mapperUnit.setResultTitle(request.getSession().getId());
        model.addAttribute(ConstantsFor.TITLE, "Navigation Test");
        model.addAttribute("MapperUnit", mapperUnit);
        model.addAttribute("pagetitle", response.getStatus() + " status<br>Encoding is " + response.getCharacterEncoding());
        model.addAttribute("content", request.getRemoteAddr() + "<br>" + response.getContentType());
        model.addAttribute(ConstantsFor.FOOTER, "Можно добавить картинки, динамические маршруты и пр.<br>" + new PageFooter().getTheFooter());
        return "nav";
    }

    @PostMapping("/navpost")
    public String navPost(Model model, @ModelAttribute MapperUnit mapperUnit) {
        this.mapperUnit = mapperUnit;
        model.addAttribute("MapperUnit", mapperUnit);
        String s = new MapCoordinateParser(mapperUnit.getUserEnt()).getResultsAsText();
        model.addAttribute(ConstantsFor.TITLE, mapperUnit.getResultTitle());
        model.addAttribute("content", s);
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getTheFooter());
        return "navpost";
    }
}
