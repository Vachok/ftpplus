// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common.usermanagement;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipal;


public class UserACLReplacerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private final Path startPath = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\testClean\\");
    
    private UserACLReplacer userACLReplacer;
    
    private UserPrincipal oldUser;
    
    private UserPrincipal newUser;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        try {
            oldUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
            newUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            this.userACLReplacer = new UserACLReplacer(oldUser, startPath, newUser);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }
    
    @Test
    public void testTestToString() {
        String toStr = userACLReplacer.toString();
        Assert.assertTrue(toStr.contains("UserACLReplacer{"));
    }
    
    @Test
    public void testRun() {
    
        try {
            Files.setOwner(startPath, oldUser);
            Assert.assertEquals(Files.getOwner(startPath), oldUser);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        userACLReplacer.run();
        try {
            UserPrincipal userPrincipal = Files.getOwner(startPath);
            Assert.assertEquals(userPrincipal, newUser);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
}