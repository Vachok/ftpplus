// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;


/**
 @see PhotoConverterSRV
 @since 21.06.2019 (0:54) */
@SuppressWarnings("ALL")
public class PhotoConverterSRVTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void convertFoto() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        String psCommands = photoConverterSRV.psCommands();
        Assert.assertFalse(psCommands.isEmpty());
        Assert.assertTrue(psCommands.contains("ImportSystemModules"));
    }
    
    @Test
    public void testPsCommands() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        String psCommandsStr = photoConverterSRV.psCommands();
        Assert.assertFalse(psCommandsStr.isEmpty());
        try {
            new File(AppComponents.getProps().getProperty(ConstantsFor.PR_ADPHOTOPATH)).listFiles();
        }
        catch (NullPointerException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testGetAdFotoFile() {
    }
    
    @Test
    public void testSetAdFotoFile() {
        throw new InvokeEmptyMethodException("17.07.2019 (21:02)");
    }
    
    @Test
    public void testToString1() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        String msg = "photoConverterSRV.toString() = " + photoConverterSRV.toString();
        Assert.assertTrue(msg.contains("PhotoConverterSRV["));
    }
}