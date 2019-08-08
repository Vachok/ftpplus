package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageFooter;


/**
 Контроллер для /AT_NAME_COMMON
 <p>

 @since 05.12.2018 (9:04) */
@Controller
public class CommonCTRL {
    
    
    private final InformationFactory pageFooter = new PageFooter();
    
    private CommonSRV commonSRV;
    
    @Contract(pure = true)
    @Autowired
    public CommonCTRL(CommonSRV commonSRV) {
        this.commonSRV = commonSRV;
    }
    
    @GetMapping("/common")
    public String commonGET(@NotNull Model model) {
        commonSRV.setNullToAllFields();
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        model.addAttribute(ModelAttributeNames.ATT_COMMON, commonSRV);
        model.addAttribute(ModelAttributeNames.ATT_HEAD, pageFooter.getInfoAbout(ModelAttributeNames.ATT_HEAD));
//        model.addAttribute(ConstantsFor.ATT_RESULT, "<details><summary>Last searched:</summary>"+new String(FileSystemWorker.readFile("CommonSRV.reStoreDir.results.txt").getBytes(), StandardCharsets.UTF_8)+"</details>");
        return ModelAttributeNames.ATT_COMMON;
    }

    @PostMapping("/commonarch")
    public String commonArchPOST(@ModelAttribute CommonSRV commonSRV, @NotNull Model model) {
        this.commonSRV = commonSRV;
        model.addAttribute(ModelAttributeNames.ATT_COMMON, commonSRV);
        model.addAttribute(ModelAttributeNames.ATT_TITLE, commonSRV.getPathToRestoreAsStr() + " (" + commonSRV.getPerionDays() + " дн.) ");
        model.addAttribute(ModelAttributeNames.ATT_RESULT, commonSRV.reStoreDir());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        return ModelAttributeNames.ATT_COMMON;
    }
    
    /**
     For tests
     <p>
     
     @param commonSRV {@link CommonSRV}
     */
    public void setCommonSRV(CommonSRV commonSRV) {
        this.commonSRV = commonSRV;
    }

    @PostMapping("/commonsearch")
    public String commonSearch(@ModelAttribute CommonSRV commonSRV, @NotNull Model model) {
        this.commonSRV = commonSRV;
        model.addAttribute(ModelAttributeNames.ATT_COMMON, commonSRV);
        String patternToSearch = commonSRV.getSearchPat();
        model.addAttribute(ModelAttributeNames.ATT_TITLE, patternToSearch + " - идёт поиск");
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        model.addAttribute(ModelAttributeNames.ATT_RESULT, commonSRV.searchByPat(patternToSearch));
        return ModelAttributeNames.ATT_COMMON;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonCTRL{");
        sb.append("pageFooter=").append(pageFooter);
        sb.append(", commonSRV=").append(commonSRV);
        sb.append('}');
        return sb.toString();
    }
}
