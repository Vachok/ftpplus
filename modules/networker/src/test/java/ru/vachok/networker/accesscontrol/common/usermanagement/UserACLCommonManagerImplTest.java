// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common.usermanagement;


import org.junit.Test;
import org.testng.Assert;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.InvokeEmptyMethodException;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    
    @Test
    public void addAccess() {
        throw new InvokeEmptyMethodException("25.07.2019 (14:11)");
    }
    
    @Test
    public void removeAccess() {
        throw new InvokeEmptyMethodException("25.07.2019 (14:12)");
    }
    
    @Test
    public void changeUsers() {
        try {
            this.newUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\newuser.txt"));
            this.oldUser = Files.getOwner(Paths.get("\\\\srv-fs\\it$$\\ХЛАМ\\userchanger\\olduser.txt"));
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
        }
        UserACLCommonManager userACLCommonManager = new UserACLCommonManagerImpl(Paths.get("\\\\srv-fs.eatmeat.ru\\it$$\\ХЛАМ\\testClean\\"));
        String changeUsers = userACLCommonManager.replaceUsers(oldUser, newUser);
    }
    
    @Test
    public void addAcl() {
        throw new TODOException("25.07.2019 (14:15)");
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (Files.getOwner(dir).equals(oldUser)) {
            Files.setOwner(dir, newUser);
            messageToUser.info(MessageFormat.format("{0}) USER SET", foldersCounter), dir.toString(), newUser.toString());
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        List<AclEntry> currentACLEntries = Files.getFileAttributeView(file, AclFileAttributeView.class).getAcl();
        List<AclEntry> neededACLEntries = new ArrayList<>();
    
        currentACLEntries.forEach((acl)->{
            if (acl.principal().equals(oldUser)) {
                try {
                    Files.setOwner(file, newUser);
                    filesACLs.add(MessageFormat.format("File: {0}\nOld ACL:\n{1}\nNew ACL:\n{2}\n\n",
                        file, new TForms().fromArray(currentACLEntries), new TForms().fromArray(neededACLEntries)));
                }
                catch (IOException e) {
                    Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e));
                }
                neededACLEntries.add(changeACL(acl));
                messageToUser.info(MessageFormat.format("{0}) FILE SET", filesCounter), file.toString(), newUser.toString());
            }
            else {
                neededACLEntries.add(acl);
            }
        });
        Files.getFileAttributeView(file, AclFileAttributeView.class).setAcl(neededACLEntries);
        this.filesCounter++;
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName() + ".err"), file + "\n" + new TForms().fromArray(exc));
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        this.foldersCounter++;
        if (!filesACLs.isEmpty()) {
            FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName() + ".res"), filesACLs);
            filesACLs.clear();
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserChanger{");
        sb.append(", oldUser='").append(oldUser).append('\'');
        sb.append(", newUser='").append(newUser.getName()).append('\'');
        sb.append(", Files visited: ").append(filesCounter).append(". Folders visited: ").append(foldersCounter).append('\'');
        sb.append('}');
        return sb.toString();
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
