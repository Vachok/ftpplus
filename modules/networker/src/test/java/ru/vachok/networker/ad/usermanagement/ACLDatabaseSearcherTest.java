package ru.vachok.networker.ad.usermanagement;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 @see ACLDatabaseSearcher
 @since 11.10.2019 (9:56) */
public class ACLDatabaseSearcherTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(ACLDatabaseSearcher.class.getSimpleName(), System.nanoTime());
    
    private long linesLimit = Integer.MAX_VALUE;
    
    private UserACLManager dbSearcher;
    
    private int countDirectories;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 2));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initSearcher() {
        this.dbSearcher = UserACLManager.getInstance(UserACLManager.DB_SEARCH, Paths.get("."));
    }
    
    @Test
    public void testGetResult() {
        String result;
        try {
            
            result = dbSearcher.getResult();
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        testSetClassOption();
        result = dbSearcher.getResult();
        Assert.assertTrue(result.contains("select * from common where user like '%kudr%' limit 2147483647"));
        Assert.assertTrue(result.contains("\\\\srv-fs.eatmeat.ru\\common_new\\Общие_документы_МК\\13_Служба_персонала\\Общая\\БП"));
        dbSearcher.setClassOption(1);
        result = dbSearcher.getResult();
        Assert.assertTrue(result.contains("select * from common where user like '%kudr%' limit 1"));
    }
    
    @Test
    public void testSetClassOption() {
        List<String> classOpt = new ArrayList<>();
        classOpt.add("kudr");
        dbSearcher.setClassOption(classOpt);
        Assert.assertTrue(dbSearcher.toString().contains("searchPatterns=1"));
    }
    
    @Test
    public void testTestToString() {
        String string = dbSearcher.toString();
        Assert.assertTrue(string.contains("ACLDatabaseSearcher{"));
    }
}