// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;


/**
 @see UserACLManagerImpl
 @since 17.07.2019 (11:44) */
public class UserACLManagerImplTest extends SimpleFileVisitor<Path> {
    
    
    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private UserPrincipal oldUser;
    
    private UserPrincipal newUser;
    
    private int filesCounter = 0;
    
    private int foldersCounter = 0;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Collection<String> filesACLs = new LinkedBlockingQueue<>();
    
    private Path startPath = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\");
    
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
    public void setFields() {
        try {
            this.newUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            this.oldUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void addAccess() {
        UserACLManager userACLManager = new UserACLManagerImpl(startPath);
        String addAccess = userACLManager.addAccess(oldUser);
        Assert.assertFalse(addAccess.isEmpty());
        System.out.println("addAccess = " + addAccess);
    }
    
    @Test
    public void removeAccess() {
        UserACLManager userACLManager = new UserACLManagerImpl(startPath);
        String removeAccess = userACLManager.removeAccess(newUser);
        Assert.assertFalse(removeAccess.isEmpty());
        System.out.println("removeAccess = " + removeAccess);
    }
    
    @Test
    public void testReplaceUsers() {
        UserACLManager userACLReplace = new UserACLManagerImpl(startPath);
        String replaceUsers = userACLReplace.replaceUsers(newUser, oldUser);
        Assert.assertFalse(replaceUsers.isEmpty());
        System.out.println("replaceUsers = " + replaceUsers);
    }
    
    @Test
    public void testCreateACLForUserFromExistsACL() {
        AclFileAttributeView attributeView = Files.getFileAttributeView(Paths.get("."), AclFileAttributeView.class);
        try {
            for (AclEntry aclEntry : attributeView.getAcl()) {
                AclEntry existsACL = UserACLManager.createACLForUserFromExistsACL(aclEntry, Files.getOwner(Paths.get("UserACLReplacer.res")));
                AclEntry newACL = UserACLManager.createNewACL(Files.getOwner(Paths.get("UserACLReplacer.res")));
                Assert.assertEquals(newACL, existsACL);
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    @Test
    public void testCreateNewACL() {
        try {
            UserACLManager.createNewACL(Files.getOwner(Paths.get(".")));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
    
    private @NotNull AclEntry changeACL(@NotNull AclEntry acl) {
        AclEntry.Builder aclBuilder = AclEntry.newBuilder();
        aclBuilder.setPermissions(acl.permissions());
        aclBuilder.setType(acl.type());
        aclBuilder.setPrincipal(newUser);
        aclBuilder.setFlags(acl.flags());
        return aclBuilder.build();
    }
}
