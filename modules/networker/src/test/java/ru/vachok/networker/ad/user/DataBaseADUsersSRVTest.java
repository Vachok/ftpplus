// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.util.List;
import java.util.Queue;


/**
 @see DataBaseADUsersSRV
 @since 21.06.2019 (13:08) */
@SuppressWarnings("ALL") public class DataBaseADUsersSRVTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private DataBaseADUsersSRV dataBaseADUsersSRV = new DataBaseADUsersSRV();
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    
    /**
     @see DataBaseADUsersSRV#fileParser(Queue)
     */
    @Test
    public void testFileParser() {
        DataBaseADUsersSRV dataBaseADUsersSRV = new DataBaseADUsersSRV();
        Queue<String> usersCsv = FileSystemWorker.readFileToQueue(new File(getClass().getResource("/users.csv").getFile()).toPath());
        dataBaseADUsersSRV.fileParser(usersCsv);
        
        List<ADUser> adUsers = dataBaseADUsersSRV.getAdUsers();
        String outString = "new TForms().fromArray(adUsers, false) = " + new TForms().fromArray(adUsers, false);
    
        Assert.assertTrue(outString.contains("kudr"), outString);
        testConfigureThreadsLogMaker.getPrintStream().println(outString);
    }
    
    @Test
    public void adUsersFromFileGetter() {
        List<ADUser> adUsers = dataBaseADUsersSRV.getAdUsers();
        Assert.assertTrue(adUsers.size() == 0);
        String file = getClass().getResource("/users.csv").getFile();
        adUsers = dataBaseADUsersSRV.getAdUsers(new File(file));
        Assert.assertTrue(adUsers.size() > 500);
    }
    
    @Test
    public void testGetAdUsers() {
        List<ADUser> adUsers = dataBaseADUsersSRV.getAdUsers();
        Assert.assertNotNull(adUsers);
    }
    
    @Test
    public void testTestToString() {
        String toStr = dataBaseADUsersSRV.toString();
        Assert.assertTrue(toStr.contains("DataBaseADUsersSRV{"), toStr);
    }
}