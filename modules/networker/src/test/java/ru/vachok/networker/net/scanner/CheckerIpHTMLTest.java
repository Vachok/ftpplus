package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.*;

import static org.testng.Assert.assertTrue;


/**
 @see CheckerIpHTML
 @since 12.07.2019 (14:55) */
public class CheckerIpHTMLTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(CheckerIpHTMLTest.class.getSimpleName(), System
        .nanoTime());
    
    private PrintStream printStream;
    
    private File fileTest = new File(this.getClass().getSimpleName() + ".test");
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initPS() {
        try (OutputStream outputStream = new FileOutputStream(fileTest)) {
            this.printStream = new PrintStream(outputStream, true);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        
    }
    
    @Test
    public void testCheckIP() {
        CheckerIpHTML checkerIpHTML = new CheckerIpHTML("10.200.213.85", printStream);
        checkerIpHTML.checkIP();
        assertTrue(fileTest.exists());
        fileTest.deleteOnExit();
    }
    
    @Test
    public void testToString1() {
        assertTrue(new CheckerIpHTML("", printStream).toString().contains("CheckerIp{"));
    }
}