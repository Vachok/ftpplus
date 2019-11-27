package ru.vachok.networker.ad.usermanagement;


import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.UserPrincipal;


/**
 @see UserACLDeleter
 @since 26.07.2019 (11:15) */
public class UserACLDeleterTest {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
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
    public void testDeleter() {
        UserPrincipal oldUser = null;
        try {
            oldUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        UserACLManager userACLManager = UserACLManager.getInstance(UserACLManager.DEL, Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\"));

//        UserACLManager userACLManager = UserACLManager.getInstance(UserACLManager.DEL, Paths.get("\\\\srv-fs\\Common_new\\Z01.ПАПКИ_ОБМЕНА\\Маркетинг-Упаковка\\"));
        String removeAccess = userACLManager.removeAccess(oldUser);
    }
}