package ru.vachok.networker.net.scanner;


import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


public class PcNamesScannerTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(NetListsTest.class.getSimpleName(), System.nanoTime());
    
    private PcNamesScanner pcNamesScanner = new PcNamesScanner();
    
    private NetScanCtr netScanCtr = new NetScanCtr(new PcNamesScanner());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
        this.pcNamesScanner.setClassOption(netScanCtr);
        this.pcNamesScanner.setThePc("do0001");
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @Test
    public void testTestToString() {
        String toStr = pcNamesScanner.toString();
        Assert.assertTrue(toStr.contains("PcNamesScanner["), toStr);
        System.out.println("toStr = " + toStr);
    }
    
    @Test
    public void testFillAttribute() {
        pcNamesScanner.fillAttribute("do0213");
    }
    
    @Test
    public void testFillWebModel() {
        this.netScanCtr.netScan(new MockHttpServletRequest(), new MockHttpServletResponse(), new ExtendedModelMap());
        String webM = pcNamesScanner.fillWebModel();
        throw new TODOException("23.08.2019 (20:24)");
    }
    
    @Test
    public void testSetClassOption() {
        String toStr = pcNamesScanner.toString();
        Assert.assertTrue(toStr.contains("model = true"), toStr);
    }
}