// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
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
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.mailserver.ExSRV;
import ru.vachok.networker.mailserver.MailRule;
import ru.vachok.networker.mailserver.RuleSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.concurrent.ConcurrentMap;


/**
 @see ru.vachok.networker.controller.ExCTRLTest
 @since 05.10.2018 (9:52) */
@Controller
public class ExCTRL {
    
    
    private static final String GET_MAP_RULESET = "/ruleset";
    
    private static final String EXCHANGE = "/exchange";
    
    private static final String F_EXCHANGE = "exchange";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExCTRL.class.getSimpleName());
    
    private final HTMLGeneration pageFooter = new PageGenerationHelper();
    
    private ExSRV exSRV;
    
    private RuleSet ruleSet;
    
    private ConcurrentMap<Integer, MailRule> localMap = UsefulUtilities.getMailRules();
    
    private String rawS;
    
    @Autowired
    public ExCTRL(ExSRV exSRV, RuleSet ruleSet) {
        localMap.clear();
        this.ruleSet = ruleSet;
        this.exSRV = exSRV;
    }
    
    @GetMapping(EXCHANGE)
    public String exchangeWorks(@NotNull Model model, HttpServletRequest request) {
        Visitor visitor = UsefulUtilities.getVis(request);
        String s = visitor.toString();
        LOGGER.warn(s);
        model.addAttribute(ModelAttributeNames.ATT_EXSRV, exSRV);
        model.addAttribute(ModelAttributeNames.AT_NAME_RULESET, ruleSet);
        try {
            model.addAttribute(ModelAttributeNames.TITLE, lastChange());
            model.addAttribute("file", exSRV.fileAsStrings());
        }
        catch (NullPointerException e) {
            model.addAttribute("file",
                    new StringBuilder()
                            .append("Необходимо запустить на сервере <b>Exchange</b> Power Shell Script:")
                            .append("<p><textarea>ImportSystemModules")
                            .append("\n")
                            .append("Get-TransportRule | fl > имя_файла</textarea></p>").toString());
        }
    
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER) + "<p>" + s);
        return F_EXCHANGE;
    }
    
    /**
     @return заголовок страницы.
     */
    private @NotNull String lastChange() {
        File file = new File(getClass().getResource("/static/texts/rules.txt").getFile());
        if (!file.exists()) {
            return "No file! " + file.getAbsolutePath();
        }
        else {
            return "From local: " + file.getAbsolutePath();
        }
    }
    
    @PostMapping(EXCHANGE)
    public String uplFile(@RequestParam MultipartFile file, @NotNull Model model) {
        exSRV.setFile(file);
        String s = new StringBuilder()
                .append("Содержимое других полей:<br><textarea>")
                .append(exSRV.getOFields())
                .append("</textarea>")
                .toString();
        String rules = MailRule.fromArrayRules(localMap, true);
        model.addAttribute(ModelAttributeNames.ATT_EXSRV, exSRV);
        model.addAttribute(ModelAttributeNames.AT_NAME_RULESET, ruleSet);
        model.addAttribute("file", rules + s);
        model.addAttribute(ModelAttributeNames.TITLE, localMap.size() + " rules in " +
                exSRV.getFile().getSize() / ConstantsFor.KBYTE + " kb file");
        model.addAttribute("otherfields", exSRV.getOFields());
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER));
        return F_EXCHANGE;
    }
    
    /**
     <b>Post-запрос</b>
     <p>
     Запрос, для устанвки полей {@link RuleSet#fromAddressMatchesPatterns} и {@link RuleSet#identity}
     
     @param ruleSet {@link RuleSet}
     @param model {@link Model}
     @return ok.html
     */
    @PostMapping(GET_MAP_RULESET)
    public String ruleSetPost(@NotNull @ModelAttribute RuleSet ruleSet, @NotNull Model model) {
        this.ruleSet = ruleSet;
        rawS = ruleSet.getIdentity() + "<br>" + ruleSet.getFromAddressMatchesPatterns() + "<p>" + ruleSet.getCopyToRuleSetter();
        model.addAttribute(ModelAttributeNames.AT_NAME_RULESET, ruleSet);
        model.addAttribute(ModelAttributeNames.TITLE, ruleSet.getIdentity());
        model.addAttribute("ok", rawS);
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER));
        
        return "ok";
    }
    
    @GetMapping("/osppst")
    public String ostPstGet(@NotNull Model model, HttpServletRequest request) {
        new AppComponents().visitor(request);
        model.addAttribute(ModelAttributeNames.HEAD, pageFooter.getFooter(ModelAttributeNames.HEAD));
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER));
        return "ok";
    }
    
    /**
     <b>GET-ответ</b>
     
     @param model {@link Model}
     @param response {@link HttpServletResponse}
     @return redirect:/ok?FromAddressMatchesPatterns
     
     @see #ruleSet
     */
    @GetMapping(GET_MAP_RULESET)
    public String ruleSetGet(@NotNull Model model, @NotNull HttpServletResponse response) {
        response.addHeader("pcs", "FromAddressMatchesPatterns");
        model.addAttribute(ModelAttributeNames.AT_NAME_RULESET, ruleSet);
        model.addAttribute("ok", rawS);
        return "redirect:/ok?FromAddressMatchesPatterns";
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExCTRL{");
        sb.append("pageFooter=").append(pageFooter);
        sb.append(", exSRV=").append(exSRV.toString());
        sb.append(", ruleSet=").append(ruleSet.toString());
        sb.append(", localMap=").append(localMap.size());
        sb.append(", rawS='").append(rawS).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
