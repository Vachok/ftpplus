package ru.vachok.networker.restapi.message;


import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;


/**
 @see MessageEmail
 @since 22.11.2019 (9:31) */
public class MessageEmailTest {
    
    
    private static final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(MessageEmailTest.class.getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @BeforeMethod
    public void initMsg() {
        this.messageToUser = MessageToUser.getInstance(MessageToUser.EMAIL, MessageEmailTest.class.getSimpleName());
    }
    
    @Test
    @Ignore
    public void testError() {
        messageToUser.error("test");
    }
    
    @Test
    @Ignore
    public void testInfo() {
        messageToUser.info("test");
    }
    
    @Test
    @Ignore
    public void testWarning() {
        messageToUser.warn("test");
    }
    
    @Test
    public void mailTransTest() {
        messageToUser.info(AbstractForms.fromArray(System.getProperties()));
    }
}