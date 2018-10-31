package ru.vachok.networker.mailserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 @since 05.10.2018 (9:52) */
@Controller
public class ExCTRL {

    private ExSRV exSRV;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExCTRL.class.getSimpleName());

    @Autowired
    public ExCTRL(ExSRV exSRV) {
        ConstantsFor.MAIL_RULES.clear();
        this.exSRV = exSRV;
    }

    @GetMapping("/exchange")
    public String exchangeWorks(Model model, HttpServletRequest request) {
        Visitor visitor = new Visitor(request);
        LOGGER.warn(visitor.toString());
        model.addAttribute("exsrv", exSRV);
        try {
            model.addAttribute("title", lastChange());
            model.addAttribute("file", exSRV.fileAsStrings());
        } catch (NullPointerException e) {
            model.addAttribute("title", "No local file!");
            model.addAttribute("file", "Не могу найти файл...");
        }

        model.addAttribute("footer", new PageFooter().getFooterUtext());
        return "exchange";
    }

    private String lastChange() {
        File file = new File(getClass().getResource("/static/texts/rules.txt").getFile());
        if (!file.exists()) return "No file! " + file.getAbsolutePath();
        else return "From local: " + file.getAbsolutePath();
    }

    @PostMapping("/exchange")
    public String uplFile(@RequestParam MultipartFile file, Model model) {
        exSRV.setFile(file);
        String s = exSRV.fileAsStrings();
        String rules = new TForms().fromArrayRules(ConstantsFor.MAIL_RULES, true);
        model.addAttribute("exsrv", exSRV);

        model.addAttribute("file", rules + s);
        model.addAttribute("title", exSRV.getClass().getSimpleName());
        return "exchange";
    }
}
