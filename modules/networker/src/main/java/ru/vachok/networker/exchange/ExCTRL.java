package ru.vachok.networker.exchange;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.componentsrepo.PageFooter;

import java.io.File;

/**
 @since 05.10.2018 (9:52) */
@Controller
public class ExCTRL {

    private ExSRV exSRV;

    public ExCTRL(ExSRV exSRV) {
        this.exSRV = exSRV;
    }

    @GetMapping("/exchange")
    public String exchangeWorks(Model model) {
        model.addAttribute("exsrv", exSRV);
        model.addAttribute("title", lastChange());
        model.addAttribute("file", exSRV.fileAsStrings(true));
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        return "exchange";
    }

    private String lastChange() {
        File file = new File(getClass().getResource("/static/texts/rules.txt").getFile());
        return "From local: " + file.getAbsolutePath();
    }

    @PostMapping("/exchange")
    public String uplFile(@RequestParam MultipartFile file, Model model) {
        exSRV.setFile(file);
        model.addAttribute("exsrv", exSRV);
        model.addAttribute("file", exSRV.fileAsStrings(false));
        model.addAttribute("title", exSRV.getClass().getSimpleName());
        return "exchange";
    }
}
