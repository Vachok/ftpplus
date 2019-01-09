package ru.vachok.networker.accesscontrol.common;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.PageFooter;

/**
 Контроллер для /AT_NAME_COMMON
 <p>

 @since 05.12.2018 (9:04) */
@Controller
public class CommonCTRL {

    @Autowired
    private CommonSRV commonSRV;

    @GetMapping("/common")
    public String commonGET(Model model) {
        commonSRV.setNullToAllFields();
        model.addAttribute(ConstantsFor.ATT_TITLE, ConstantsFor.percToEnd());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute(ConstantsFor.ATT_COMMON, commonSRV);
        return ConstantsFor.ATT_COMMON;
    }

    @PostMapping("/commonarch")
    public String commonArchPOST(@ModelAttribute CommonSRV commonSRV, Model model) {
        this.commonSRV = commonSRV;
        model.addAttribute(ConstantsFor.ATT_COMMON, commonSRV);
        model.addAttribute(ConstantsFor.ATT_TITLE, commonSRV.getDelFolderPath() + " (" + commonSRV.getPerionDays() + " дн.) ");
        model.addAttribute(ConstantsFor.ATT_RESULT, commonSRV.reStoreDir());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return ConstantsFor.ATT_COMMON;
    }

    @PostMapping("/commonsearch")
    public String commonSearch(@ModelAttribute CommonSRV commonSRV, Model model) {
        this.commonSRV = commonSRV;
        model.addAttribute(ConstantsFor.ATT_COMMON, commonSRV);
        model.addAttribute(ConstantsFor.ATT_TITLE, commonSRV.getSearchPat() + " - идёт поиск");
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute(ConstantsFor.ATT_RESULT, commonSRV.searchByPat());
        return ConstantsFor.ATT_COMMON;
    }
}
