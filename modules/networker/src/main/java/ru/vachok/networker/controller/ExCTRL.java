// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;



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
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.mailserver.RuleSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.concurrent.ConcurrentMap;


/**
 @since 05.10.2018 (9:52) */
@Controller
public class ExCTRL {

    private static final String GET_MAP_RULESET = "/ruleset";

    private static final String EXCHANGE = "/exchange";

    private static final String F_EXCHANGE = "exchange";

    private ExSRV exSRV;

    private RuleSet ruleSet;

    private ConcurrentMap<Integer, MailRule> localMap = ConstantsFor.getMailRules();

    private static final Logger LOGGER = LoggerFactory.getLogger(ExCTRL.class.getSimpleName());
    
    private String rawS;

    @Autowired
    public ExCTRL(ExSRV exSRV, RuleSet ruleSet) {
        localMap.clear();
        this.ruleSet = ruleSet;
        this.exSRV = exSRV;
    }

    @GetMapping (EXCHANGE)
    public String exchangeWorks(Model model, HttpServletRequest request) {
        Visitor visitor = ConstantsFor.getVis(request);
        String s = visitor.toString();
        LOGGER.warn(s);
        model.addAttribute(ConstantsFor.ATT_EXSRV, exSRV);
        model.addAttribute(ConstantsFor.AT_NAME_RULESET, ruleSet);
        try {
            model.addAttribute(ConstantsFor.ATT_TITLE, lastChange());
            model.addAttribute("file", exSRV.fileAsStrings());
        } catch (NullPointerException e) {
            model.addAttribute("file",
                new StringBuilder()
                    .append("Необходимо запустить на сервере <b>Exchange</b> Power Shell Script:")
                    .append("<p><textarea>ImportSystemModules")
                    .append("\n")
                    .append("Get-TransportRule | fl > имя_файла</textarea></p>").toString());
        }

        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<p>" + s);
        return F_EXCHANGE;
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
     2. <i>file</i>, {@link TForms} - парсер массива {@link ConstantsFor#MAIL_RULES}
     3. <i>{@link ConstantsFor#ATT_TITLE}</i>, {@link ConstantsFor#MAIL_RULES}.size()
     4. <i>otherfields</i>, {@link ExSRV#getOFields()}
     5. <i>footer</i>, {@link PageFooter#getFooterUtext()}
     @see ExSRV
     @param file {@link MultipartFile}, загружаемый пользователем через web-форму
     @param model {@link Model}
     @return exchange.html
     */
    @PostMapping (EXCHANGE)
    public String uplFile(@RequestParam MultipartFile file, Model model) {
        exSRV.setFile(file);
        String s = new StringBuilder()
            .append("Содержимое других полей:<br><textarea>")
            .append(exSRV.getOFields())
            .append("</textarea>")
            .toString();
        String rules = MailRule.fromArrayRules(localMap, true);
        model.addAttribute(ConstantsFor.ATT_EXSRV, exSRV);
        model.addAttribute(ConstantsFor.AT_NAME_RULESET, ruleSet);
        model.addAttribute("file", rules + s);
        model.addAttribute(ConstantsFor.ATT_TITLE, localMap.size() + " rules in " +
            exSRV.getFile().getSize() / ConstantsFor.KBYTE + " kb file");
        model.addAttribute("otherfields", exSRV.getOFields());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return F_EXCHANGE;
    }

    /**
     <b>Post-запрос</b>
     <p>
     Запрос, для устанвки полей {@link RuleSet#fromAddressMatchesPatterns} и {@link RuleSet#identity}

     @param ruleSet {@link RuleSet}
     @param model   {@link Model}
     @return ok.html
     */
    @PostMapping (GET_MAP_RULESET)
    public String ruleSetPost(@ModelAttribute RuleSet ruleSet, Model model) {
        this.ruleSet = ruleSet;
        rawS = ruleSet.getIdentity() + "<br>" + ruleSet.getFromAddressMatchesPatterns() + "<p>" + ruleSet.getCopyToRuleSetter();
        model.addAttribute(ConstantsFor.AT_NAME_RULESET, ruleSet);
        model.addAttribute(ConstantsFor.ATT_TITLE, ruleSet.getIdentity());
        model.addAttribute("ok", rawS);
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());

        return "ok";
    }

    /**<b>GET-ответ</b>
     @see #ruleSet
     @param model {@link Model}
     @param response {@link HttpServletResponse}
     @return redirect:/ok?FromAddressMatchesPatterns
     */
    @GetMapping (GET_MAP_RULESET)
    public String ruleSetGet(Model model, HttpServletResponse response) {
        response.addHeader("pcs", "FromAddressMatchesPatterns");
        model.addAttribute(ConstantsFor.AT_NAME_RULESET, ruleSet);
        model.addAttribute("ok", rawS);
        return "redirect:/ok?FromAddressMatchesPatterns";
    }
    
    @GetMapping("/osppst")
    public String ostPstGet(Model model, HttpServletRequest request) {
        new AppComponents().visitor(request);
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "ok";
    }
}
