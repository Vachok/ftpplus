// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Queue;


/**
 @see FileADUsersParser
 @since 21.06.2019 (13:08) */
@SuppressWarnings("ALL")
public class FileADUsersParserTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private FileADUsersParser adUsersParser = new FileADUsersParser();
    
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
    public void testToString() {
        String toStr = adUsersParser.toString();
        System.out.println("toStr = " + toStr);
    }
    
    @Test
    public void testGetADUsers() {
        Path path = new File(getClass().getResource("/users.csv").getFile()).toPath();
        Queue<String> stringsUsers = FileSystemWorker.readFileEncodedToQueue(path, "UTF-16LE");
        List<ADUser> users = adUsersParser.getADUsers(stringsUsers);
        Assert.assertTrue(users.size() > 1000);
    }
}