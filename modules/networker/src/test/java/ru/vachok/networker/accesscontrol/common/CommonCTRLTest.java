package ru.vachok.networker.accesscontrol.common;


import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 @since 17.06.2019 (10:57) */
public class CommonCTRLTest {
    
    
    @Test
    public void testCommonGET() {
        Model model = new ExtendedModelMap();
        CommonSRV commonSRV = new CommonSRV();
        CommonCTRL ctrl = new CommonCTRL();
        ctrl.setCommonSRV(commonSRV);
        String commonGETStr = ctrl.commonGET(model);
        assertTrue(commonGETStr.equals("common"));
        assertEquals(model.asMap().get("common"), commonSRV);
        assertTrue(model.asMap().size() >= 3);
    }
    
    @Test
    public void testCommonArchPOST() {
        Model model = new ExtendedModelMap();
        CommonSRV commonSRV = new CommonSRV();
        CommonCTRL ctrl = new CommonCTRL();
        commonSRV.setPerionDays("100");
        commonSRV.setPathToRestoreAsStr("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\");
        String commonArchPOSTStr = ctrl.commonArchPOST(commonSRV, model);
        assertEquals(commonArchPOSTStr, ConstantsFor.ATT_COMMON);
        assertTrue(new File("CommonSRV.reStoreDir.results.txt").lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        
        commonSRV.setNullToAllFields();
        model.asMap().clear();
        commonSRV.setPerionDays("100");
        commonSRV.setPathToRestoreAsStr("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\График отпусков 2019г  IT.XLSX");
        commonArchPOSTStr = ctrl.commonArchPOST(commonSRV, model);
        assertEquals(commonArchPOSTStr, ConstantsFor.ATT_COMMON);
        assertEquals(model.asMap().get("title"), "\\\\srv-fs\\Common_new\\14_ИТ_служба\\Общая\\График отпусков 2019г  IT.XLSX (100 дн.) ");
        assertTrue(new File("CommonSRV.reStoreDir.results.txt").lastModified() > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
    }
    
    @Test
    public void testCommonSearch() {
        Model model = new ExtendedModelMap();
        CommonSRV commonSRV = new CommonSRV();
        CommonCTRL ctrl = new CommonCTRL();
        String commonSearchStr = ctrl.commonSearch(commonSRV, model);
        assertEquals(commonSearchStr, ConstantsFor.ATT_COMMON);
        assertEquals(model.asMap().get("common"), commonSRV);
        assertEquals(model.asMap().get("title"), "null - идёт поиск");
    }
}