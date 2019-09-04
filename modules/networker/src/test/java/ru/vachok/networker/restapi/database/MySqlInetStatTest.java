package ru.vachok.networker.restapi.database;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.sql.Connection;
import java.sql.SQLException;


/**
 @see MySqlInetStat
 @since 04.09.2019 (17:07) */
public class MySqlInetStatTest {
    
    
    private static final TestConfigure TEST_CONFIGURE_THREADS_LOG_MAKER = new TestConfigureThreadsLogMaker(MySqlInetStat.class
            .getSimpleName(), System.nanoTime());
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
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
    public void testGetDataSource() {
        throw new InvokeEmptyMethodException("testGetDataSource created 04.09.2019 (17:07)");
    }
    
    @Test
    public void testGetDefaultConnection() {
        try (Connection defaultConnection = new MySqlInetStat().getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            boolean defaultConnectionValid = defaultConnection.isValid(10);
            Assert.assertTrue(defaultConnectionValid);
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".testGetDefaultConnection", e));
        }
    }
    
    @Test
    public void testTestToString() {
        throw new InvokeEmptyMethodException("testTestToString created 04.09.2019 (17:07)");
    }
}