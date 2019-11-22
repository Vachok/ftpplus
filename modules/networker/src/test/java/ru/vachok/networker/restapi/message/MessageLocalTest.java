package ru.vachok.networker.restapi.message;


import com.eclipsesource.json.*;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.TimeUnit;


/**
 @see MessageLocal
 @since 13.08.2019 (9:17) */
public class MessageLocalTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(MessageLocal.class
            .getSimpleName(), System.nanoTime());
    
    private MessageLocal messageToUser;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        TEST_CONFIGURE_THREADS_LOG_MAKER.before();
    }
    
    @AfterClass
    public void tearDown() {
        TEST_CONFIGURE_THREADS_LOG_MAKER.after();
    }
    
    @BeforeMethod
    public void initMsgToUser() {
        this.messageToUser = new MessageLocal(this.getClass().getSimpleName());
    }
    
    @Test
    public void testIgExc() {
        try {
            messageToUser.igExc(new InvokeEmptyMethodException("test"));
        }
        catch (InvokeEmptyMethodException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testInfoTimer() {
        try {
            messageToUser.infoTimer(10, "test");
        }
        catch (InvokeIllegalException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testInfo() {
        messageToUser.info("test");
        messageToUser.info("test", "test", "test");
    }
    
    @Test
    public void testError() {
        messageToUser.errorAlert("test", "test", "test");
        messageToUser.error("test");
        messageToUser.error("test", "test", "test");
    }
    
    @Test
    public void testWarn() {
        messageToUser.warn("test");
        messageToUser.warning("test");
        
        messageToUser.warning("test", "test", "test");
        messageToUser.warn("test", "test", "test");
    }
    
    @Test
    public void testLoggerFine() {
        messageToUser.loggerFine("test");
    }
    
    @Test
    public void testTestToString() {
        String toString = messageToUser.toString();
        Assert.assertTrue(toString.contains("MessageLocal{"), toString);
    }
    
    @Test
    public void testWrireLogToFile() {
        File file = new File(FileNames.APP_JSON);
        if (file.exists()) {
            Assert.assertTrue(file.delete());
        }
        messageToUser.errorAlert("test", "test", "test");
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        Assert.assertTrue(file.exists());
        Queue<String> appJson = FileSystemWorker.readFileToQueue(file.toPath().normalize().toAbsolutePath());
        while (!appJson.isEmpty()) {
            String logStr = appJson.remove();
            try {
                JsonObject jsonObject = Json.parse(logStr).asObject();
                long timestamp = jsonObject.getLong(PropertiesNames.TIMESTAMP, 0);
                Assert.assertTrue(timestamp + TimeUnit.SECONDS.toMillis(10) > System.currentTimeMillis());
            }
            catch (ParseException e) {
                Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
            }
            file.deleteOnExit();
        }
    }
}