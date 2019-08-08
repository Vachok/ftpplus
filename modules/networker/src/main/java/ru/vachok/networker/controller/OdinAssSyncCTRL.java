package ru.vachok.networker.controller;


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
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageFooter;
import ru.vachok.networker.services.CsvTxt;


/**
 @since 11.10.2018 (9:12) */
@Controller
public class OdinAssSyncCTRL {

    /**
     {@link LoggerFactory}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OdinAssSyncCTRL.class.getSimpleName());

    /**
     <i>Boiler Plate</i>
     */
    private static final String STR_ODINASS = "odinass";

    /**
     <i>Boiler Plate</i>
     */
    private static final String GET_ODINASS = "/odinass";
    
    private final InformationFactory pageFooter = new PageFooter();
    
    /**
     {@link CsvTxt}
     */
    private CsvTxt csvTxt = new CsvTxt();
    
    @PostMapping (GET_ODINASS)
    public String uploadFiles(@RequestParam MultipartFile file, RedirectAttributes redirectAttributes, Model model) {
        csvTxt.setFile(file);
        LOGGER.info(csvTxt.readFileToString());
        model.addAttribute("CsvTxt", csvTxt);
        model.addAttribute("mapfiles", mapFiles());
        model.addAttribute("csvparse", new TForms().fromArray(csvTxt.getPsCommandsList(), true));
        model.addAttribute(ModelAttributeNames.ATT_RESULT, getResult());
        return STR_ODINASS;
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

    @GetMapping (GET_ODINASS)
    public String viewPage(Model model) {
        model.addAttribute("CsvTxt", csvTxt);
        model.addAttribute(ModelAttributeNames.ATT_TITLE, OdinAssSyncCTRL.class.getSimpleName());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        return STR_ODINASS;
    }

    @GetMapping("/resetform")
    public String resetForm() {
        csvTxt.getFiles().clear();
        csvTxt.getTxtList().clear();
        csvTxt.getXlsList().clear();
        return "redirect:/odinass";
    }
}
