package ru.vachok.networker.net.scanner;


import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;

import java.io.*;

import static org.testng.Assert.assertTrue;


/**
 @see CheckerIp
 @since 12.07.2019 (14:55) */
public class CheckerIpTest {
    
    
    private PrintStream printStream;
    
    private File fileTest = new File(this.getClass().getSimpleName() + ".test");
    
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
        CheckerIp checkerIp = new CheckerIp("10.200.213.85", printStream);
        checkerIp.checkIP();
        assertTrue(fileTest.exists());
        fileTest.deleteOnExit();
    }
    
    @Test
    public void testToString1() {
        assertTrue(new CheckerIp("", printStream).toString().contains("CheckerIp{"));
    }
    
    @Test
    public void checkSWThread() {
        CheckerIp checkerIp = new CheckerIp("", printStream);
        checkerIp.checkSwitchesAvail();
    }
}