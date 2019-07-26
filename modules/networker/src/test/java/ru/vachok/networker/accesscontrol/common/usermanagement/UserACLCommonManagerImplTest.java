// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;


/**
 @since 17.07.2019 (11:44)
 @see UserACLCommonManagerImpl
 */
public class UserACLCommonManagerImplTest extends SimpleFileVisitor<Path> {
    
    
    private UserPrincipal oldUser;
    
    private UserPrincipal newUser;
    
    private int filesCounter = 0;
    
    private int foldersCounter = 0;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private Collection<String> filesACLs = new LinkedBlockingQueue<>();
    
    private Path startPath = Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\testClean\\");
    
    @Test
    public void addAccess() {
        throw new InvokeEmptyMethodException("25.07.2019 (14:11)");
    }
    
    @Test
    public void removeAccess() {
        try {
            this.oldUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        UserACLCommonManager userACLCommonManager = new UserACLCommonManagerImpl(startPath);
        String removeAccess = userACLCommonManager.removeAccess(oldUser);
        Assert.assertFalse(removeAccess.isEmpty());
    }
    
    @Test
    public void changeUsers() {
        try {
            this.newUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            this.oldUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
            System.out.println("newUser.toString() = " + newUser.toString().split(" ")[0]);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        UserACLCommonManager userACLCommonManager = new UserACLCommonManagerImpl(startPath);
        String changeUsers = userACLCommonManager.replaceUsers(oldUser, newUser);
    }
    
    private AclEntry changeACL(AclEntry acl) {
        AclEntry.Builder aclBuilder = AclEntry.newBuilder();
        aclBuilder.setPermissions(acl.permissions());
        aclBuilder.setType(acl.type());
        aclBuilder.setPrincipal(newUser);
        aclBuilder.setFlags(acl.flags());
        return aclBuilder.build();
    }
}
