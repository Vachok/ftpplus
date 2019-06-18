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
    
    
    private CommonSRV commonSRV;
    
    @Autowired
    public CommonCTRL(CommonSRV commonSRV) {
        this.commonSRV = commonSRV;
    }
    
    protected CommonCTRL() {
    }
    
    @GetMapping("/common")
    public String commonGET(Model model) {
        commonSRV.setNullToAllFields();
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute(ConstantsFor.ATT_COMMON, commonSRV);
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
//        model.addAttribute(ConstantsFor.ATT_RESULT, "<details><summary>Last searched:</summary>"+new String(FileSystemWorker.readFile("CommonSRV.reStoreDir.results.txt").getBytes(), StandardCharsets.UTF_8)+"</details>");
        return ConstantsFor.ATT_COMMON;
    }

    @PostMapping("/commonarch")
    public String commonArchPOST(@ModelAttribute CommonSRV commonSRV, Model model) {
        this.commonSRV = commonSRV;
        model.addAttribute(ConstantsFor.ATT_COMMON, commonSRV);
        model.addAttribute(ConstantsFor.ATT_TITLE, commonSRV.getPathToRestoreAsStr() + " (" + commonSRV.getPerionDays() + " дн.) ");
        model.addAttribute(ConstantsFor.ATT_RESULT, commonSRV.reStoreDir());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return ConstantsFor.ATT_COMMON;
    }
    
    /**
     For tests
     <p>
     
     @param commonSRV {@link CommonSRV}
     */
    protected void setCommonSRV(CommonSRV commonSRV) {
        this.commonSRV = commonSRV;
    }

    @PostMapping("/commonsearch")
    public String commonSearch(@ModelAttribute CommonSRV commonSRV, Model model) {
        this.commonSRV = commonSRV;
        model.addAttribute(ConstantsFor.ATT_COMMON, commonSRV);
        String patternToSearch = commonSRV.getSearchPat();
        model.addAttribute(ConstantsFor.ATT_TITLE, patternToSearch + " - идёт поиск");
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        model.addAttribute(ConstantsFor.ATT_RESULT, commonSRV.searchByPat(patternToSearch));
        return ConstantsFor.ATT_COMMON;
    }
}
