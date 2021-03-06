// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.props.InitProperties;

import java.io.File;


/**
 @see PhotoConverterSRV
 @since 21.06.2019 (0:54) */
@SuppressWarnings("ALL")
public class PhotoConverterSRVTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
    
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
        PhotoConverterSRV photoConverterSRV = this.photoConverterSRV;
        String psCommands = photoConverterSRV.psCommands();
        Assert.assertFalse(psCommands.isEmpty());
        Assert.assertTrue(psCommands.contains(ConstantsFor.PS_IMPORTSYSMODULES));
    }
    
    @Test
    public void testPsCommands() {
        PhotoConverterSRV photoConverterSRV = this.photoConverterSRV;
        String psCommandsStr = photoConverterSRV.psCommands();
        Assert.assertFalse(psCommandsStr.isEmpty());
        try {
            new File(InitProperties.getTheProps().getProperty(PropertiesNames.ADPHOTOPATH)).listFiles();
        }
        catch (NullPointerException e) {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testGetAdFotoFile() {
        photoConverterSRV.setAdFotoFile(new File(getClass().getResource("/tests/20180705_211750.jpg").getFile()));
        File adFotoFile = photoConverterSRV.getAdFotoFile();
        Assert.assertTrue(adFotoFile.exists());
    }
    
    @Test
    public void testToString1() {
        PhotoConverterSRV photoConverterSRV = this.photoConverterSRV;
        String msg = "photoConverterSRV.toString() = " + photoConverterSRV.toString();
        Assert.assertTrue(msg.contains("PhotoConverterSRV["));
    }
}