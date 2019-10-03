package ru.vachok.networker.data.synchronizer;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;

import java.util.Collections;
import java.util.Map;


/**
 @see OnOffTable
 @since 30.09.2019 (13:49) */
public class OnOffTableTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(OnOffTableTest.class.getSimpleName(), System.nanoTime());
    
    private OnOffTable offTable;
    
    @BeforeMethod
    public void initClasse() {
        this.offTable = new OnOffTable();
    }
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testGetDbToSync() {
        String dbToSync = offTable.getDbToSync();
        Assert.assertEquals(dbToSync, ConstantsFor.DB_ONOFF);
    }
    
    @Test
    public void testSetDbToSync() {
        try {
            offTable.setDbToSync("");
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            Assert.assertEquals(e.getMessage(), "velkom.onoff is constant!");
        }
    }
    
    @Test
    public void testSetOption() {
        try {
            offTable.setOption("");
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            Assert.assertEquals(e.getMessage(), "velkom.onoff is constant!");
        }
    }
    
    /**
     @see OnOffTable#syncData()
     */
    @Test
    @Ignore
    public void testSyncData() {
        String sData = offTable.syncData();
        Assert.assertTrue(sData.contains("no0001.eatmeat.ru  Online ="));
    }
    
    @Test
    public void testSuperRun() {
        try {
            offTable.superRun();
            
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
    }
    
    @Test
    public void testUploadCollection() {
        try {
            int i = offTable.uploadCollection(Collections.singletonList("test"), "test.test");
            Assert.assertTrue(i > 0);
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
    }
    
    @Test
    public void testMakeColumns() {
        try {
            Map<String, String> map = offTable.makeColumns();
            Assert.assertFalse(map.isEmpty());
        }
        catch (TODOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
    }
    
    @Test
    public void testTestToString() {
        String s = offTable.toString();
        Assert.assertTrue(s.contains("OnOffTable["));
    }
}