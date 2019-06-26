// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TestConfigure;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 @see CommonCTRL
 @since 17.06.2019 (10:57) */
@SuppressWarnings("ALL") public class CommonCTRLTest {
    
    
    private final TestConfigure testConfigure = new TestConfigure(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }

    
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
    
    /**
     @see CommonCTRL#commonArchPOST(CommonSRV, Model)
     */
    @Test(timeOut = 350000)
    public void testCommonArchPOST() {
        Model model = new ExtendedModelMap();
        CommonSRV commonSRV = new CommonSRV();
        CommonCTRL ctrl = new CommonCTRL();
        commonSRV.setPerionDays("100");
        commonSRV.setPathToRestoreAsStr("\\\\srv-fs\\Common_new\\14_ИТ_служба\\");
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
    
    /**
     @see CommonCTRL#commonSearch(CommonSRV, Model)
     */
    @Test(timeOut = 90000)
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