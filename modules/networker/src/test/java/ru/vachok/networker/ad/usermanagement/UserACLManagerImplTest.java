// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.usermanagement;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
        this.userACLManager = UserACLManager.getInstance(UserACLManager.ADD, startPath);
        try {
            String addAccess = userACLManager.addAccess(newUser);
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        String addAccessStr = UserACLManagerImpl.addAccess(newUser, startPath);
        Assert.assertTrue(addAccessStr.contains("added"));
    }
    
    @Test
    public void removeAccess() {
        this.userACLManager = UserACLManager.getInstance(UserACLManager.DEL, startPath);
        try {
            String removeAccess = userACLManager.removeAccess(newUser);
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        String removeResult = UserACLManagerImpl.removeAccess(newUser, startPath);
        Assert.assertTrue(removeResult.contains("testClean removed"));
    }
    
    @Test
    public void testReplaceUsers() {
        this.userACLManager = UserACLManager.getInstance("", startPath);
        try {
            String replaceUsers = userACLManager.replaceUsers(newUser, oldUser);
        }
        catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
        String replaceResults = UserACLManagerImpl.replaceUsers(oldUser, startPath, newUser);
        Assert.assertTrue(replaceResults.contains("testClean users changed"));
    }
    
    @Test
    public void testCreateACLForUserFromExistsACL() {
        AclFileAttributeView attributeView = Files.getFileAttributeView(Paths.get("."), AclFileAttributeView.class);
        try {
            for (AclEntry aclEntry : attributeView.getAcl()) {
                AclEntry existsACL = UserACLManagerImpl.createACLForUserFromExistsACL(aclEntry, Files.getOwner(Paths.get("UpakFilesTest.res")));
                AclEntry newACL = UserACLManagerImpl.createACLFor(Files.getOwner(Paths.get("UpakFilesTest.res")), "rw");
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
        UserPrincipal principalToSet = null;
        Path pathToTest = Paths.get("\\\\rups00.eatmeat.ru\\c$\\Users\\ikudryashov\\Documents\\lan");
        Path pathToSetACL = Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\userchanger\\");
    
        try {
            principalToSet = Files.getOwner(pathToTest);
        
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    
        AclFileAttributeView ownerACL = Files.getFileAttributeView(pathToTest, AclFileAttributeView.class);
        AclFileAttributeView toSetACL = Files.getFileAttributeView(pathToSetACL, AclFileAttributeView.class);
        List<AclEntry> checkList = new ArrayList<>();
    
        try {
            List<AclEntry> entries = checkPathToSet(toSetACL, principalToSet);
            Files.getFileAttributeView(pathToSetACL, AclFileAttributeView.class).setAcl(entries);
            checkList.addAll(entries);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        String checkString = new TForms().fromArray(checkList);
        Assert.assertTrue(checkString.contains(principalToSet.getName()));
    }
    
    private @NotNull List<AclEntry> checkPathToSet(@NotNull AclFileAttributeView toSetACL, UserPrincipal principalToSet) throws InvalidObjectException {
        AclEntry acl = UserACLManagerImpl.createACLFor(principalToSet, "rw");
        try {
            List<AclEntry> aclListToSET = Collections.synchronizedList(toSetACL.getAcl());
            List<AclEntry> newAclList = new ArrayList<>();
            
            for (AclEntry aclEntry : aclListToSET) {
                if (!aclEntry.principal().equals(principalToSet)) {
                    newAclList.add(aclEntry);
                }
            }
            String aclsString = new TForms().fromArray(newAclList);
            Assert.assertFalse(aclsString.contains(principalToSet.getName()), aclsString);
            newAclList.add(acl);
            aclsString = new TForms().fromArray(newAclList);
            Assert.assertTrue(aclsString.contains(principalToSet.getName()), aclsString + "\n\ntoSET: " + acl.toString());
            return newAclList;
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        throw new InvalidObjectException(acl.toString());
    }
    
    private @NotNull AclEntry changeACL(@NotNull AclEntry acl) {
        AclEntry.Builder aclBuilder = AclEntry.newBuilder();
        aclBuilder.setPermissions(acl.permissions());
        aclBuilder.setType(acl.type());
        aclBuilder.setPrincipal(newUser);
        aclBuilder.setFlags(acl.flags());
        return aclBuilder.build();
    }
    
    @Test
    public void testAdd() {
        UserACLManager userACLManager = UserACLManager.getInstance(UserACLManager.ADD, Paths
            .get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\testClean\\"));
        try {
            UserPrincipal principal = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            String addAccess = userACLManager.addAccess(principal);
            System.out.println("addAccess = " + addAccess);
        }
        catch (UnsupportedOperationException | IOException e) {
            Assert.assertNotNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
    }
}
