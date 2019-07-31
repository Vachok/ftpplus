// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;
import java.util.Queue;


public class UserACLReplacerTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private UserACLReplacer userACLReplacer;
    
    private UserPrincipal oldUser;
    
    private UserPrincipal newUser;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    @BeforeClass
    public void setUp() {
        try {
            oldUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
            newUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            this.userACLReplacer = new UserACLReplacer(oldUser, Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\testClean\\"), newUser);
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
        userACLReplacer.run();
    }
    
    @Test(enabled = false)
    public void exp$$AclFromFile() {
        Queue<String> foldersWithACL = FileSystemWorker.readFileToQueue(Paths.get("folders").toAbsolutePath().normalize());
        while (!foldersWithACL.isEmpty()) {
            replaceACL(foldersWithACL.poll());
            messageToUser.warn(foldersWithACL.size() + " foldersWithACL size.");
        }
    }
    
    private void replaceACL(@NotNull String folderWithACL) {
        Path path = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\testClean\\");
        
        if (!folderWithACL.isEmpty()) {
            path = Paths.get(folderWithACL).normalize();
        }
        
        UserACLReplacer userACLReplacer = new UserACLReplacer(oldUser, path, newUser);
        userACLReplacer.setFollowLinks(1);
        userACLReplacer.run();
    }
}