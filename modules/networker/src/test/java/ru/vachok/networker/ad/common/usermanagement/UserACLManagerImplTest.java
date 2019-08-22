// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.common.usermanagement;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
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
    
    private UserACLManager userACLManager;
    
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
        this.userACLManager = UserACLManager.getI(UserACLManager.ADD, startPath);
        String addAccess = userACLManager.addAccess(oldUser);
        Assert.assertFalse(addAccess.isEmpty());
        System.out.println("addAccess = " + addAccess);
    }
    
    @Test
    public void removeAccess() {
        this.userACLManager = UserACLManager.getI(UserACLManager.DEL, startPath);
        String removeAccess = userACLManager.removeAccess(newUser);
        Assert.assertFalse(removeAccess.isEmpty());
        System.out.println("removeAccess = " + removeAccess);
    }
    
    @Test
    public void testReplaceUsers() {
        this.userACLManager = UserACLManager.getI("", startPath);
        String replaceUsers = userACLManager.replaceUsers(newUser, oldUser);
        Assert.assertFalse(replaceUsers.isEmpty());
        System.out.println("replaceUsers = " + replaceUsers);
    }
    
    @Test
    public void testCreateACLForUserFromExistsACL() {
        AclFileAttributeView attributeView = Files.getFileAttributeView(Paths.get("."), AclFileAttributeView.class);
        try {
            for (AclEntry aclEntry : attributeView.getAcl()) {
                AclEntry existsACL = UserACLManager.createACLForUserFromExistsACL(aclEntry, Files.getOwner(Paths.get("UpakFilesTest.res")));
                AclEntry newACL = UserACLManager.createNewACL(Files.getOwner(Paths.get("UpakFilesTest.res")));
                Assert.assertTrue(newACL.toString()
                        .contains("READ_DATA/WRITE_DATA/APPEND_DATA/READ_NAMED_ATTRS/WRITE_NAMED_ATTRS/EXECUTE/DELETE_CHILD/READ_ATTRIBUTES/WRITE_ATTRIBUTES/DELETE/READ_ACL/WRITE_ACL/WRITE_OWNER/SYNCHRONIZE"), newACL
                        .toString());
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
