package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 @since 11.10.2018 (9:12) */
@Controller
public class OdinAssSyncCTRL {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdinAssSyncCTRL.class.getSimpleName());

    private CsvTxt csvTxt = new CsvTxt();

    @PostMapping("/odinass")
    public String uploadFiles(@RequestParam MultipartFile file, RedirectAttributes redirectAttributes, Model model) {
        csvTxt.setFile(file);
        LOGGER.info(csvTxt.readFileToString());
        model.addAttribute("CsvTxt", csvTxt);
        model.addAttribute("mapfiles", mapFiles());
        model.addAttribute("csvparse", new TForms().fromArray(csvTxt.getPsCommandsList()));
        model.addAttribute("result", getResult());
        return "odinass";
    }

    private String mapFiles() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("<center><b><h3><a href=\"/resetform\">RESET FILES</b></h3></a></center>")
            .append(new TForms().fromArray(csvTxt.getFiles(), true));
        return stringBuilder.toString();

    }

    private String getResult() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h2><center>Здесь будет город-сад</center></h2>");
        stringBuilder.append("<p>");
        return stringBuilder.toString();
    }

    @GetMapping("/odinass")
    public String viewPage(Model model) {
        model.addAttribute("CsvTxt", csvTxt);
        model.addAttribute("title", OdinAssSyncCTRL.class.getSimpleName());
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        return "odinass";
    }

    @GetMapping("/resetform")
    public String resetForm() {
        csvTxt.getFiles().clear();
        csvTxt.getTxtList().clear();
        csvTxt.getXlsList().clear();
        return "redirect:/odinass";
    }

    private void getCSV(HttpServletResponse response) {
        try (ServletOutputStream outputStream = response.getOutputStream()) {
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
