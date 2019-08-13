// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.File;
import java.util.List;
import java.util.Random;


/**
 @see FileADUsersParser
 @since 21.06.2019 (13:08) */
@SuppressWarnings("ALL")
public class FileADUsersParserTest {
    
    
    private final TestConfigureThreadsLogMaker testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private UserInformation dataBaseADUsersSRV = new FileADUsersParser();
    
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
    public void testTestToString() {
        String toStr = dataBaseADUsersSRV.toString();
        Assert.assertTrue(toStr.contains("DataBaseADUsersSRV{"), toStr);
    }
    
    @Test
    public void testGetAdUsers() {
        dataBaseADUsersSRV.setInfo(new File(getClass().getResource("/users.csv").getFile()).toPath());
        List<ADUser> adUsers = dataBaseADUsersSRV.getADUsers();
        int size = adUsers.size();
        Assert.assertTrue(size > 0);
        ADUser user = adUsers.get(new Random().nextInt(size));
        System.out.println("user = " + user.getSamAccountName());
    }
    
    @Test
    public void testGetInfoAbout() {
        dataBaseADUsersSRV.setInfo(new File(getClass().getResource("/users.csv").getFile()));
        String infoAbout = dataBaseADUsersSRV.getInfoAbout("rbibishev");
        System.out.println("infoAbout = " + infoAbout);
    }
    
    @Test
    public void testSetInfo() {
    }
    
    @Test
    public void testGetADUsers() {
    }
}