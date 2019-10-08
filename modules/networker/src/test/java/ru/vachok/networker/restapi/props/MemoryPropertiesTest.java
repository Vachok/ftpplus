package ru.vachok.networker.restapi.props;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.*;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class MemoryPropertiesTest {
    
    
    private static final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(MemoryProperties.class.getSimpleName(), System.nanoTime());
    
    private MemoryProperties memoryProperties;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void initMP() {
        this.memoryProperties = (MemoryProperties) InitProperties.getInstance(InitProperties.DB);
    }
    
    @Test
    public void testGetProps() {
        Properties memoryPropertiesProps = memoryProperties.getProps();
        Assert.assertTrue(memoryPropertiesProps.size() > 10);
        Assert.assertTrue(memoryPropertiesProps.containsKey(PropertiesNames.DBPASS));
        Assert.assertTrue(memoryPropertiesProps.containsKey(PropertiesNames.DBUSER));
        Assert.assertTrue(memoryPropertiesProps.containsKey("spring.servlet.multipart.max-request-size"));
        Assert.assertTrue(new File(ConstantsFor.class.getSimpleName() + FileNames.EXT_PROPERTIES).exists());
        Assert.assertTrue(new File(ConstantsFor.class.getSimpleName() + FileNames.EXT_PROPERTIES).lastModified() > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)));
    }
    
    @Test
    public void testSetProps() {
        Properties properties = InitProperties.getInstance(InitProperties.FILE).getProps();
        Assert.assertTrue(properties.size() > 17);
        boolean isSetProps = memoryProperties.setProps(properties);
        Assert.assertTrue(isSetProps, AbstractForms.fromArray(properties));
    }
    
    @Test
    public void testDelProps() {
    }
}