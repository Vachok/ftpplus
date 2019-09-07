// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.sql.*;
import java.text.MessageFormat;
import java.util.concurrent.*;


/**
 @since 17.06.2019 (14:41) */
public class OldBigFilesInfoCollectorTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final OldBigFilesInfoCollector infoCollector = new OldBigFilesInfoCollector();
    
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
    public void testCall() {
        String startPath = infoCollector.getStartPath();
        Assert.assertEquals(startPath, "\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\testClean\\");
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(infoCollector);
        String callY2K = "null";
        
        try {
            callY2K = submit.get(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void testGetStartPath() {
        String startPath = infoCollector.getStartPath();
        Assert.assertEquals(startPath, "\\\\srv-fs.eatmeat.ru\\Common_new\\14_ИТ_служба\\Общая\\testClean\\");
    }
    
    @Test
    public void testTestToString() {
        Assert.assertTrue(infoCollector.toString().contains("Common2Years25MbytesInfoCollector{"), infoCollector.toString());
    }
    
    @Test
    @Ignore
    public void realCall(){
        OldBigFilesInfoCollector oldBigFilesInfoCollector = new OldBigFilesInfoCollector();
        oldBigFilesInfoCollector.call();
    }
    
    @Test
    public void testInDB(){
        try(Connection connection = DataConnectTo.getInstance(DataConnectTo.LOC_INETSTAT).getDefaultConnection("velkom");
            PreparedStatement preparedStatement = connection.prepareStatement("select * from oldfiles");
            ResultSet resultSet = preparedStatement.executeQuery()){
            while(resultSet.next()){
                System.out.println(MessageFormat.format("{0} is {1} {2} megabytes",resultSet.getInt(1), resultSet.getString(2), resultSet.getFloat(3)));
                System.out.println(resultSet.getString(3));
            }
        }
        catch (SQLException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}