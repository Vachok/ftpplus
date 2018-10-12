package ru.vachok.networker.exchange;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 @since 05.10.2018 (9:52) */
@Controller
public class ExCTRL {

    private static final String UPLOADED_FOLDER = "/static/texts/";

    private static final String TITLE_STR = "title";

    private static final String EXCHANGE_STRING = "exchange";

    private ExSRV exSRV = new AppComponents().exSRV();

    @GetMapping(EXCHANGE_STRING)
    public String exController(Model model) {
        model.addAttribute(TITLE_STR, System.currentTimeMillis());
        model.addAttribute("exsrv", exSRV);
        model.addAttribute("rulesbean", exSRV.getRulesBean());
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        return EXCHANGE_STRING;
    }

    @PostMapping("/exchupl")
    public String upFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes, Model model) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
        }
        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);
            redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded '" + file.getOriginalFilename() + "'");
        } catch (IOException e) {
            model.addAttribute("file", e.getMessage() + new TForms().fromArray(e, true));
        }
        exSRV.setFile(file);
        modUpl(model);
        return "redirect:exchupl";
    }

    @GetMapping("/exchupl")
    public String modUpl(Model model) {
        model.addAttribute("exsrv", exSRV);
        model.addAttribute(TITLE_STR,
            exSRV.getFile().getOriginalFilename() +
                " uploaded. " +
                exSRV.getFile().getSize() +
                " bytes of " +
                exSRV.getFile().getContentType() +
                ".");
        model.addAttribute("file",
            exSRV.getFile().getSize() +
                " " +
                exSRV.getFile().getContentType() +
                " " +
                exSRV.getFile().getOriginalFilename());
        model.addAttribute("rulesbean", exSRV.getRulesBean());
        model.addAttribute("transport", exSRV.getRulesFromFile());
        model.addAttribute("query", exSRV.getRulesBean().getQuery());
        return "exchupl";
    }

    @PostMapping("/exchuplsrch")
    public String srchRules(Model model) {
        model.addAttribute(TITLE_STR, exSRV.getRulesBean().getAllRules().size());
        model.addAttribute("query", exSRV.getRulesBean().getQuery());
        return "redirect:/exchupl";
    }
}
