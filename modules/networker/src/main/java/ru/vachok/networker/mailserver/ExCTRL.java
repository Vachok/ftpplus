package ru.vachok.networker.mailserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 @since 05.10.2018 (9:52) */
@Controller
public class ExCTRL {

    private ExSRV exSRV;

    private RuleSet ruleSet;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExCTRL.class.getSimpleName());

    private String rawS;
    @Autowired
    public ExCTRL(ExSRV exSRV, RuleSet ruleSet) {
        ConstantsFor.MAIL_RULES.clear();
        this.ruleSet = ruleSet;
        this.exSRV = exSRV;
    }

    /**
     <b>/exchange (GET)</b>
     Модель. Атрибуты:<br> 1. {@link ExSRV} <br> 2. {@link #lastChange()} <br> 3. {@link ExSRV#fileAsList} <br> 4. Статичный текст при пустом файле. <br> 5. {@link PageFooter}

     @param model   {@link Model}, на запрос "/exchange"
     @param request {@link HttpServletRequest}
     @return exchange.html
     */
    @GetMapping("/exchange")
    public String exchangeWorks(Model model, HttpServletRequest request) {
        Visitor visitor = new Visitor(request);
        LOGGER.warn(visitor.toString());
        model.addAttribute("exsrv", exSRV);
        model.addAttribute("ruleset", ruleSet);
        try {
            model.addAttribute(ConstantsFor.TITLE, lastChange());
            model.addAttribute("file", exSRV.fileAsStrings());
        } catch (NullPointerException e) {
            model.addAttribute("file",
                new StringBuilder()
                    .append("Необходимо запустить на сервере <b>Exchange</b> Power Shell Script:")
                    .append("<p><textarea>ImportSystemModules")
                    .append("\n")
                    .append("Get-TransportRule | fl > имя_файла</textarea></p>").toString());
        }

        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
        return "exchange";
    }

    /**
     @return заголовок страницы.
     */
    private String lastChange() {
        File file = new File(getClass().getResource("/static/texts/rules.txt").getFile());
        if (!file.exists()) return "No file! " + file.getAbsolutePath();
        else return "From local: " + file.getAbsolutePath();
    }

    /**<b>/exchange (POST)</b>
     Модель. <br>
     1. <i>exSRV</i>, {@link ExSRV} <br>
     2. <i>file</i>, {@link TForms} - парсер массива {@link ConstantsFor#MAIL_RULES}
     3. <i>{@link ConstantsFor#TITLE}</i>, {@link ConstantsFor#MAIL_RULES}.size()
     4. <i>otherfields</i>, {@link ExSRV#getOFields()}
     5. <i>footer</i>, {@link PageFooter#getFooterUtext()}
     @see ExSRV
     @param file {@link MultipartFile}, загружаемый пользователем через web-форму
     @param model {@link Model}
     @return exchange.html
     */
    @PostMapping("/exchange")
    public String uplFile(@RequestParam MultipartFile file, Model model) {
        exSRV.setFile(file);
        String s = new StringBuilder()
            .append("Содержимое других полей:<br><textarea>")
            .append(exSRV.getOFields())
            .append("</textarea>")
            .toString();
        String rules = new TForms().fromArrayRules(ConstantsFor.MAIL_RULES, true);
        model.addAttribute("exsrv", exSRV);
        model.addAttribute("ruleset", ruleSet);
        model.addAttribute("file", rules + s);
        model.addAttribute(ConstantsFor.TITLE, ConstantsFor.MAIL_RULES.size() + " rules in " +
            exSRV.getFile().getSize() / ConstantsFor.KBYTE + " kb file");
        model.addAttribute("otherfields", exSRV.getOFields());
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        return "exchange";
    }

    @PostMapping("/ruleset")
    public String ruleSet(@ModelAttribute RuleSet ruleSet, Model model) {
        this.ruleSet = ruleSet;
        rawS = ruleSet.getIdentity() + "<br>" + ruleSet.getFromAddressMatchesPatterns() + "<p>" + ruleSet.getCopyToRuleSetter();
        model.addAttribute("ruleset", ruleSet);
        model.addAttribute("title", ruleSet.getIdentity());
        model.addAttribute("ok", rawS);
        model.addAttribute("footer", new PageFooter().getFooterUtext());

        return "ok";
    }

    @GetMapping("/ruleset")
    public String getRuleSet(Model model, HttpServletResponse response) {
        response.addHeader("pcs", "FromAddressMatchesPatterns");
        model.addAttribute("ruleset", ruleSet);
        model.addAttribute("ok", rawS);
        return "redirect:/ok?FromAddressMatchesPatterns";
    }
}
